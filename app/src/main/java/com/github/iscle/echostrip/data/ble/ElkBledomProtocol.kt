package com.github.iscle.echostrip.data.ble

import com.github.iscle.echostrip.domain.model.Rgb
import com.github.iscle.echostrip.domain.model.StripTimer
import com.github.iscle.echostrip.domain.model.TimerAction
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.UUID

/**
 * The plain (unencrypted) ELK-BLEDOM protocol, as used by duoCo Strip for devices that
 * advertise as `ELK-…` without the `*` that marks encrypted firmware. Every frame is
 * `7E <len> <cmd> … EF`, written to characteristic FFF3 of service FFF0.
 */
object ElkBledomProtocol {
    val ServiceUuid: UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")
    val WriteCharacteristicUuid: UUID = UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb")

    /** Name prefixes the strip advertises; a `*` in the name means encrypted firmware. */
    val NamePrefixes = listOf("ELK-", "ELK~", "LED LIGHT STRIP", "XSL-")

    fun isSupportedName(name: String?): Boolean =
        name != null && '*' !in name && NamePrefixes.any { name.startsWith(it) }

    fun power(on: Boolean) = StripCommand(
        "power",
        if (on) frame(0x04, 0x04, 0xE0, 0x00, 0x01, 0xFF, 0x00) else frame(0x04, 0x04, 0x00, 0x00, 0x00, 0xFF, 0x00),
    )

    fun color(rgb: Rgb) = StripCommand("color", frame(0x07, 0x05, 0x03, rgb.red, rgb.green, rgb.blue, 0x10))

    /** [percent] is clamped to 0..100. */
    fun brightness(percent: Int) =
        StripCommand("brightness", frame(0x04, 0x01, percent.coerceIn(0, 100), 0xFF, 0xFF, 0xFF, 0x00))

    /** Sets the strip's clock, which its timers run on. Weekday is 0 = Sunday … 6 = Saturday. */
    fun clock(now: LocalDateTime) = StripCommand(
        "clock",
        frame(0x07, 0x83, now.hour, now.minute, now.second, now.dayOfWeek.value % 7, 0xFF),
    )

    /** Mode 0 is the turn-on timer, mode 1 the turn-off timer. */
    fun timer(timer: StripTimer): StripCommand {
        val mode = if (timer.action == TimerAction.TurnOn) 0 else 1
        val flags = weekMask(timer.days) or (if (timer.isEnabled) 0x80 else 0)
        return StripCommand("timer-$mode", frame(0x08, 0x82, timer.hour, timer.minute, 0x00, mode, flags))
    }

    /** Bit 0 is Monday through bit 6 for Sunday. */
    fun weekMask(days: Set<DayOfWeek>): Int = days.fold(0) { mask, day -> mask or (1 shl (day.value - 1)) }

    private fun frame(vararg body: Int): ByteArray {
        val bytes = ByteArray(9)
        bytes[0] = 0x7E
        body.forEachIndexed { i, v -> bytes[i + 1] = v.toByte() }
        bytes[8] = 0xEF.toByte()
        return bytes
    }
}
