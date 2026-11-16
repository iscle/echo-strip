package com.github.iscle.echostrip.presentation.shell

import com.github.iscle.echostrip.domain.model.StripTimer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class NextEvent(val timer: StripTimer, val at: LocalDateTime)

/** The soonest moment one of the enabled [timers] will fire after [now], if any. */
fun nextEvent(timers: List<StripTimer>, now: LocalDateTime): NextEvent? =
    timers.filter { it.isEnabled }
        .mapNotNull { timer -> nextFiring(timer, now)?.let { NextEvent(timer, it) } }
        .minByOrNull { it.at }

private fun nextFiring(timer: StripTimer, now: LocalDateTime): LocalDateTime? {
    val time = LocalTime.of(timer.hour, timer.minute)
    return (0..7).asSequence()
        .map { now.toLocalDate().plusDays(it.toLong()) }
        .map { LocalDateTime.of(it, time) }
        .firstOrNull { it.isAfter(now) && (timer.days.isEmpty() || it.dayOfWeek in timer.days) }
}

/** "today", "tomorrow" or the weekday name. */
fun relativeDay(date: LocalDate, today: LocalDate): String = when (date) {
    today -> "today"
    today.plusDays(1) -> "tomorrow"
    else -> date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
}
