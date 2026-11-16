package com.github.iscle.echostrip.presentation.shell

import androidx.annotation.DrawableRes
import com.github.iscle.echostrip.R
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Top-level panes, picked from the rail. */
@Serializable
sealed interface Destination : NavKey {
    val title: String
    @get:DrawableRes val icon: Int

    @Serializable
    data object Light : Destination {
        override val title get() = "Light"
        override val icon get() = R.drawable.ic_light
    }

    @Serializable
    data object Schedule : Destination {
        override val title get() = "Schedule"
        override val icon get() = R.drawable.ic_schedule
    }

    companion object {
        val entries = listOf(Light, Schedule)
    }
}
