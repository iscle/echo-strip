package com.github.iscle.echostrip.data.local

import com.github.iscle.echostrip.data.ble.ElkBledomProtocol
import com.github.iscle.echostrip.domain.model.LightState
import com.github.iscle.echostrip.domain.model.Rgb
import com.github.iscle.echostrip.domain.model.StripTimer
import com.github.iscle.echostrip.domain.model.TimerAction
import java.time.DayOfWeek

fun LightStateEntity.toDomain() = LightState(isOn, Rgb.fromPacked(color), brightness)

fun LightState.toEntity() = LightStateEntity(isOn = isOn, color = color.packed, brightness = brightness)

fun TimerEntity.toDomain() = StripTimer(
    action = if (mode == 0) TimerAction.TurnOn else TimerAction.TurnOff,
    isEnabled = isEnabled,
    hour = hour,
    minute = minute,
    days = DayOfWeek.entries.filter { days and (1 shl (it.value - 1)) != 0 }.toSet(),
)

fun StripTimer.toEntity() = TimerEntity(
    mode = if (action == TimerAction.TurnOn) 0 else 1,
    isEnabled = isEnabled,
    hour = hour,
    minute = minute,
    days = ElkBledomProtocol.weekMask(days),
)
