package com.github.iscle.echostrip.domain.model

sealed interface ConnectionState {
    /** Bluetooth is off, missing, or the app lacks permission to use it. */
    data object Unavailable : ConnectionState

    /** Idle: the app is in the background or has not started looking yet. */
    data object Disconnected : ConnectionState

    data object Searching : ConnectionState

    data class Connecting(val name: String) : ConnectionState

    data class Connected(val name: String) : ConnectionState
}
