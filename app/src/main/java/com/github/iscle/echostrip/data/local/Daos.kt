package com.github.iscle.echostrip.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LightStateDao {
    @Query("SELECT * FROM light_state WHERE id = 0")
    fun observe(): Flow<LightStateEntity?>

    @Query("SELECT * FROM light_state WHERE id = 0")
    suspend fun get(): LightStateEntity?

    @Upsert
    suspend fun upsert(entity: LightStateEntity)
}

@Dao
interface TimerDao {
    @Query("SELECT * FROM timer ORDER BY mode")
    fun observeAll(): Flow<List<TimerEntity>>

    @Query("SELECT * FROM timer ORDER BY mode")
    suspend fun getAll(): List<TimerEntity>

    @Upsert
    suspend fun upsert(entity: TimerEntity)
}

@Dao
interface DeviceDao {
    @Query("SELECT * FROM device WHERE id = 0")
    suspend fun get(): DeviceEntity?

    @Upsert
    suspend fun upsert(entity: DeviceEntity)
}
