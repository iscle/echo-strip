package com.github.iscle.echostrip.presentation.common

import com.github.iscle.echostrip.domain.model.StripTimer
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

fun formatTime(hour: Int, minute: Int) = "%02d:%02d".format(hour, minute)

fun DayOfWeek.shortLabel(): String = getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2)

fun DayOfWeek.fullLabel(): String = getDisplayName(TextStyle.FULL, Locale.getDefault())

/** "Every day", "Weekdays", "Mon, Wed" — or "Once" when no day is picked. */
fun describeDays(days: Set<DayOfWeek>): String = when (days) {
    StripTimer.EveryDay -> "Every day"
    StripTimer.Weekdays -> "Weekdays"
    setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) -> "Weekends"
    emptySet<DayOfWeek>() -> "Once, then off"
    else -> DayOfWeek.entries.filter { it in days }
        .joinToString(", ") { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
}
