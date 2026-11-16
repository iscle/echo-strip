package com.github.iscle.echostrip.data.repository

import com.github.iscle.echostrip.data.ble.ElkBledomProtocol
import com.github.iscle.echostrip.data.ble.KnownStrip
import com.github.iscle.echostrip.data.ble.StripLink
import com.github.iscle.echostrip.data.local.DeviceDao
import com.github.iscle.echostrip.data.local.DeviceEntity
import com.github.iscle.echostrip.data.local.TimerDao
import com.github.iscle.echostrip.data.local.toDomain
import com.github.iscle.echostrip.domain.model.ConnectionState
import com.github.iscle.echostrip.domain.repository.StripRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime

/**
 * Owns the strip session. Each time the link comes up it remembers the strip and brings its
 * clock and timers up to date, since the strip loses its clock whenever it loses power.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class StripRepositoryImpl(
    private val link: StripLink,
    private val deviceDao: DeviceDao,
    private val timerDao: TimerDao,
    private val scope: CoroutineScope,
) : StripRepository {
    override val connectionState: StateFlow<ConnectionState> = link.state

    private var startJob: Job? = null

    init {
        scope.launch {
            link.connections.collect { strip ->
                deviceDao.upsert(DeviceEntity(address = strip.address, name = strip.name))
                link.send(ElkBledomProtocol.clock(LocalDateTime.now()))
                // Only timers set from this app are pushed, so ones made elsewhere survive a first run.
                timerDao.getAll().forEach { link.send(ElkBledomProtocol.timer(it.toDomain())) }
                Timber.d("Synced clock and timers with %s", strip.name)
            }
        }
    }

    override fun connect() {
        startJob?.cancel()
        startJob = scope.launch {
            val known = deviceDao.get()?.let { KnownStrip(it.address, it.name) }
            link.start(known)
        }
    }

    override fun disconnect() {
        startJob?.cancel()
        link.stop()
    }
}
