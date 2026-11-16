package com.github.iscle.echostrip.domain.model

import java.time.DayOfWeek

enum class TimerAction { TurnOn, TurnOff }

/**
 * One of the two timers stored on the strip itself, so they run even when the Echo is asleep.
 * An enabled timer with no days fires once and then disables itself on the strip.
 */
data class StripTimer(
    val action: TimerAction,
    val isEnabled: Boolean,
    val hour: Int,
    val minute: Int,
    val days: Set<DayOfWeek>,
) {
    init {
        require(hour in 0..23 && minute in 0..59) { "Invalid time $hour:$minute" }
    }

    companion object {
        val Weekdays = DayOfWeek.entries.take(5).toSet()
        val EveryDay = DayOfWeek.entries.toSet()

        val Defaults = listOf(
            StripTimer(TimerAction.TurnOn, isEnabled = false, hour = 7, minute = 0, days = Weekdays),
            StripTimer(TimerAction.TurnOff, isEnabled = false, hour = 23, minute = 30, days = EveryDay),
        )
    }
}
