package com.github.iscle.echostrip.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LightStateEntity::class, TimerEntity::class, DeviceEntity::class],
    version = 1,
)
abstract class StripDatabase : RoomDatabase() {
    abstract fun lightStateDao(): LightStateDao
    abstract fun timerDao(): TimerDao
    abstract fun deviceDao(): DeviceDao

    companion object {
        fun open(context: Context): StripDatabase =
            Room.databaseBuilder(context, StripDatabase::class.java, "strip.db").build()
    }
}
