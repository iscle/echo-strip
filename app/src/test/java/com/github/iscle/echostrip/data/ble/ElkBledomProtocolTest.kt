package com.github.iscle.echostrip.data.ble

import com.github.iscle.echostrip.domain.model.Rgb
import com.github.iscle.echostrip.domain.model.StripTimer
import com.github.iscle.echostrip.domain.model.TimerAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

class ElkBledomProtocolTest {

    private fun StripCommand.hex() = bytes.joinToString(" ") { "%02X".format(it) }

    @Test
    fun `power frames match duoCo Strip`() {
        assertEquals("7E 04 04 E0 00 01 FF 00 EF", ElkBledomProtocol.power(true).hex())
        assertEquals("7E 04 04 00 00 00 FF 00 EF", ElkBledomProtocol.power(false).hex())
    }

    @Test
    fun `color frame carries the channels in RGB order`() {
        assertEquals("7E 07 05 03 FF 77 00 10 EF", ElkBledomProtocol.color(Rgb(255, 119, 0)).hex())
    }

    @Test
    fun `brightness is clamped to a percentage`() {
        assertEquals("7E 04 01 50 FF FF FF 00 EF", ElkBledomProtocol.brightness(80).hex())
        assertEquals("7E 04 01 64 FF FF FF 00 EF", ElkBledomProtocol.brightness(250).hex())
    }

    @Test
    fun `clock counts weekdays from Sunday`() {
        // 2026-11-16 is a Monday.
        val frame = ElkBledomProtocol.clock(LocalDateTime.of(2026, 11, 16, 21, 42, 5))
        assertEquals("7E 07 83 15 2A 05 01 FF EF", frame.hex())
        val sunday = ElkBledomProtocol.clock(LocalDateTime.of(2026, 11, 22, 0, 0, 0))
        assertEquals(0, sunday.bytes[6].toInt())
    }

    @Test
    fun `timer packs mode, weekdays and the enabled bit`() {
        val on = StripTimer(TimerAction.TurnOn, isEnabled = true, hour = 7, minute = 0, days = StripTimer.Weekdays)
        assertEquals("7E 08 82 07 00 00 00 9F EF", ElkBledomProtocol.timer(on).hex())

        val off = StripTimer(TimerAction.TurnOff, isEnabled = false, hour = 23, minute = 30, days = setOf(DayOfWeek.SUNDAY))
        assertEquals("7E 08 82 17 1E 00 01 40 EF", ElkBledomProtocol.timer(off).hex())
    }

    @Test
    fun `timers coalesce per mode`() {
        val on = StripTimer.Defaults[0]
        val off = StripTimer.Defaults[1]
        assertFalse(ElkBledomProtocol.timer(on).key == ElkBledomProtocol.timer(off).key)
    }

    @Test
    fun `encrypted and unknown names are rejected`() {
        assertTrue(ElkBledomProtocol.isSupportedName("ELK-BLEDOM   "))
        assertFalse(ElkBledomProtocol.isSupportedName("ELK-*BLEDOM"))
        assertFalse(ElkBledomProtocol.isSupportedName("Xiaomi 15"))
        assertFalse(ElkBledomProtocol.isSupportedName(null))
    }
}
