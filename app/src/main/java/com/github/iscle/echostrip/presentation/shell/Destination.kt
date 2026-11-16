package com.github.iscle.echostrip.presentation.shell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessAlarm
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Top-level panes, picked from the rail. */
@Serializable
sealed interface Destination : NavKey {
    val title: String
    val icon: ImageVector

    @Serializable
    data object Light : Destination {
        override val title get() = "Light"
        override val icon get() = Icons.Outlined.Lightbulb
    }

    @Serializable
    data object Schedule : Destination {
        override val title get() = "Schedule"
        override val icon get() = Icons.Outlined.AccessAlarm
    }

    companion object {
        val entries = listOf(Light, Schedule)
    }
}
