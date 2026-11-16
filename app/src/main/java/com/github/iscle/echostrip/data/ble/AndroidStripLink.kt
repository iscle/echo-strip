package com.github.iscle.echostrip.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.github.iscle.echostrip.domain.model.ConnectionState
import com.github.iscle.echostrip.platform.BluetoothPermissions
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

/**
 * Every field below is touched only from [linkDispatcher], a single thread, so GATT callbacks,
 * the write queue and start/stop never race. Callbacks hop onto it before doing anything.
 */
@SuppressLint("MissingPermission") // start() refuses to run without BluetoothPermissions.
@OptIn(ExperimentalCoroutinesApi::class)
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class AndroidStripLink(
    private val context: Context,
    private val scope: CoroutineScope,
) : StripLink {
    private val linkDispatcher = Dispatchers.IO.limitedParallelism(1)
    private val adapter: BluetoothAdapter? =
        context.getSystemService(BluetoothManager::class.java)?.adapter

    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val state = _state.asStateFlow()

    private val _connections = MutableSharedFlow<KnownStrip>(extraBufferCapacity = 1)
    override val connections = _connections.asSharedFlow()

    private var active = false
    private var known: KnownStrip? = null
    private var directFailures = 0
    private var scanning = false
    private var gatt: BluetoothGatt? = null
    private var target: KnownStrip? = null
    private var characteristic: BluetoothGattCharacteristic? = null
    private var pendingAck: CompletableDeferred<Unit>? = null
    private var timeoutJob: Job? = null
    private var retryJob: Job? = null

    private val pending = LinkedHashMap<String, ByteArray>()
    private val wake = Channel<Unit>(Channel.CONFLATED)

    init {
        scope.launch(linkDispatcher) { for (signal in wake) drainQueue() }
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val adapterState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                onLink {
                    if (!active) return@onLink
                    when (adapterState) {
                        BluetoothAdapter.STATE_ON -> begin()
                        BluetoothAdapter.STATE_OFF -> { teardown(); _state.value = ConnectionState.Unavailable }
                    }
                }
            }
        }, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    override fun start(known: KnownStrip?) = onLink {
        this.known = known
        directFailures = 0
        active = true
        begin()
    }

    override fun stop() = onLink {
        active = false
        teardown()
        _state.value = ConnectionState.Disconnected
        Timber.d("Link stopped")
    }

    override fun send(command: StripCommand) = onLink {
        pending.remove(command.key)
        pending[command.key] = command.bytes
        wake.trySend(Unit)
    }

    private fun onLink(block: () -> Unit) {
        scope.launch(linkDispatcher) { block() }
    }

    private fun begin() {
        teardown()
        val adapter = adapter
        if (adapter == null || !adapter.isEnabled || !BluetoothPermissions.granted(context)) {
            Timber.w("Bluetooth unavailable (adapter=%s, permissions=%s)", adapter?.isEnabled, BluetoothPermissions.granted(context))
            _state.value = ConnectionState.Unavailable
            return
        }
        val known = known
        // A saved address skips the scan, but strips can change address, so fall back after two misses.
        if (known != null && directFailures < 2) {
            connect(adapter.getRemoteDevice(known.address), known.name)
        } else {
            scan()
        }
    }

    private fun scan() {
        val scanner = adapter?.bluetoothLeScanner ?: return retryLater()
        _state.value = ConnectionState.Searching
        scanning = true
        Timber.d("Scanning for a strip")
        scanner.startScan(null, ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), scanCallback)
        timeoutJob = scope.launch(linkDispatcher) {
            delay(SCAN_TIMEOUT_MS)
            Timber.d("No strip found, trying again")
            stopScan()
            retryLater()
        }
    }

    private fun stopScan() {
        if (!scanning) return
        scanning = false
        runCatching { adapter?.bluetoothLeScanner?.stopScan(scanCallback) }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val name = result.scanRecord?.deviceName ?: result.device.name
            if (!ElkBledomProtocol.isSupportedName(name)) return
            onLink {
                if (!scanning) return@onLink
                Timber.i("Found %s at %s (rssi %d)", name, result.device.address, result.rssi)
                stopScan()
                timeoutJob?.cancel()
                connect(result.device, name!!.trim())
            }
        }

        override fun onScanFailed(errorCode: Int) = onLink {
            Timber.w("Scan failed: %d", errorCode)
            scanning = false
            retryLater()
        }
    }

    private fun connect(device: BluetoothDevice, name: String) {
        _state.value = ConnectionState.Connecting(name)
        target = KnownStrip(device.address, name)
        Timber.d("Connecting to %s (%s)", name, device.address)
        gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        timeoutJob = scope.launch(linkDispatcher) {
            delay(CONNECT_TIMEOUT_MS)
            Timber.w("Connection to %s timed out", device.address)
            onConnectionLost()
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) = onLink {
            if (g != gatt) return@onLink
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                Timber.d("Connected, discovering services")
                g.discoverServices()
            } else {
                Timber.w("Disconnected (status %d, state %d)", status, newState)
                onConnectionLost()
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) = onLink {
            if (g != gatt) return@onLink
            val found = g.getService(ElkBledomProtocol.ServiceUuid)
                ?.getCharacteristic(ElkBledomProtocol.WriteCharacteristicUuid)
            val strip = target
            if (found == null || strip == null) {
                Timber.e("Strip has no FFF0/FFF3 characteristic")
                onConnectionLost()
                return@onLink
            }
            timeoutJob?.cancel()
            characteristic = found
            directFailures = 0
            known = strip
            _state.value = ConnectionState.Connected(strip.name)
            Timber.i("Ready: %s", strip)
            _connections.tryEmit(strip)
            wake.trySend(Unit)
        }

        override fun onCharacteristicWrite(g: BluetoothGatt, c: BluetoothGattCharacteristic, status: Int) = onLink {
            pendingAck?.complete(Unit)
        }
    }

    private fun onConnectionLost() {
        if (known != null) directFailures++
        teardown()
        if (active) {
            _state.value = ConnectionState.Searching
            retryLater()
        }
    }

    private fun retryLater() {
        retryJob?.cancel()
        retryJob = scope.launch(linkDispatcher) {
            delay(RETRY_DELAY_MS)
            if (active) begin()
        }
    }

    private fun teardown() {
        timeoutJob?.cancel()
        retryJob?.cancel()
        stopScan()
        characteristic = null
        pendingAck?.complete(Unit)
        gatt?.let {
            runCatching { it.disconnect() }
            runCatching { it.close() }
        }
        gatt = null
    }

    private suspend fun drainQueue() {
        while (true) {
            val g = gatt ?: return
            val c = characteristic ?: return
            val (key, bytes) = pending.entries.firstOrNull()?.toPair() ?: return
            pending.remove(key)
            val ack = CompletableDeferred<Unit>().also { pendingAck = it }
            if (!write(g, c, bytes)) {
                Timber.w("Write of %s refused, will retry", key)
                pending.putIfAbsent(key, bytes)
                delay(WRITE_GAP_MS * 4)
                continue
            }
            Timber.v("Sent %s", StripCommand(key, bytes))
            // Writes without response may never be acknowledged; the timeout keeps the queue moving.
            withTimeoutOrNull(ACK_TIMEOUT_MS) { ack.await() }
            delay(WRITE_GAP_MS)
        }
    }

    @Suppress("DEPRECATION")
    private fun write(g: BluetoothGatt, c: BluetoothGattCharacteristic, bytes: ByteArray): Boolean {
        val type = if (c.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) {
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        } else {
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            g.writeCharacteristic(c, bytes, type) == BluetoothStatusCodes.SUCCESS
        } else {
            c.writeType = type
            c.value = bytes
            g.writeCharacteristic(c)
        }
    }

    private companion object {
        const val SCAN_TIMEOUT_MS = 20_000L
        const val CONNECT_TIMEOUT_MS = 15_000L
        const val RETRY_DELAY_MS = 3_000L
        const val ACK_TIMEOUT_MS = 250L
        const val WRITE_GAP_MS = 20L
    }
}
