package com.github.iscle.echostrip.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A single row: the last state sent to the strip. */
@Entity(tableName = "light_state")
data class LightStateEntity(
    @PrimaryKey val id: Int = 0,
    val isOn: Boolean,
    /** 0xRRGGBB. */
    val color: Int,
    val brightness: Int,
)

/** One row per strip timer, keyed by the protocol's mode (0 = turn on, 1 = turn off). */
@Entity(tableName = "timer")
data class TimerEntity(
    @PrimaryKey val mode: Int,
    val isEnabled: Boolean,
    val hour: Int,
    val minute: Int,
    /** Bit 0 = Monday … bit 6 = Sunday. */
    val days: Int,
)

/** The strip last connected to, so later launches can skip the scan. */
@Entity(tableName = "device")
data class DeviceEntity(
    @PrimaryKey val id: Int = 0,
    val address: String,
    val name: String,
)
