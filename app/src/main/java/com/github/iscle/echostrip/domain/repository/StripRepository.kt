package com.github.iscle.echostrip.domain.repository

import com.github.iscle.echostrip.domain.model.ConnectionState
import kotlinx.coroutines.flow.StateFlow

/** The Bluetooth link to the strip. */
interface StripRepository {
    val connectionState: StateFlow<ConnectionState>

    /** Finds the strip and keeps a connection open until [disconnect]. */
    fun connect()

    /** Releases the strip so other controllers (a phone, the remote app) can reach it. */
    fun disconnect()
}
