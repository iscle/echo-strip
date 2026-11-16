package com.github.iscle.echostrip.data.ble

import com.github.iscle.echostrip.domain.model.ConnectionState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

data class KnownStrip(val address: String, val name: String)

/** A GATT connection to one strip, kept alive while started. */
interface StripLink {
    val state: StateFlow<ConnectionState>

    /** Emits each time a connection becomes ready to take commands. */
    val connections: SharedFlow<KnownStrip>

    /** Connects to [known] when given, and scans for any supported strip otherwise. */
    fun start(known: KnownStrip?)

    fun stop()

    /** Queues [command]; a queued command with the same key is replaced. Sent once connected. */
    fun send(command: StripCommand)
}
