package com.github.iscle.echostrip.presentation.shell

import com.github.iscle.echostrip.domain.model.StripTimer
import com.github.iscle.echostrip.domain.model.TimerAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

class NextEventTest {
    // A Monday evening.
    private val now = LocalDateTime.of(2026, 11, 16, 21, 42)

    private fun timer(action: TimerAction, h: Int, m: Int, days: Set<DayOfWeek>, enabled: Boolean = true) =
        StripTimer(action, enabled, h, m, days)

    @Test
    fun `picks the sooner of the two timers`() {
        val on = timer(TimerAction.TurnOn, 7, 0, StripTimer.Weekdays)
        val off = timer(TimerAction.TurnOff, 23, 30, StripTimer.EveryDay)
        val next = nextEvent(listOf(on, off), now)!!
        assertEquals(TimerAction.TurnOff, next.timer.action)
        assertEquals(LocalDateTime.of(2026, 11, 16, 23, 30), next.at)
    }

    @Test
    fun `skips days the timer does not run on`() {
        val on = timer(TimerAction.TurnOn, 7, 0, setOf(DayOfWeek.SATURDAY))
        assertEquals(LocalDateTime.of(2026, 11, 21, 7, 0), nextEvent(listOf(on), now)!!.at)
    }

    @Test
    fun `a one-off timer fires at the next occurrence of its time`() {
        val on = timer(TimerAction.TurnOn, 6, 15, emptySet())
        assertEquals(LocalDateTime.of(2026, 11, 17, 6, 15), nextEvent(listOf(on), now)!!.at)
    }

    @Test
    fun `disabled timers are ignored`() {
        assertNull(nextEvent(StripTimer.Defaults, now))
    }

    @Test
    fun `relative day names`() {
        val today = now.toLocalDate()
        assertEquals("today", relativeDay(today, today))
        assertEquals("tomorrow", relativeDay(today.plusDays(1), today))
        assertEquals("Thursday", relativeDay(today.plusDays(3), today))
    }
}
