package com.github.iscle.echostrip.domain.repository

import com.github.iscle.echostrip.domain.model.StripTimer
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    /** Always one timer per action, turn-on first. */
    val timers: Flow<List<StripTimer>>

    suspend fun saveTimer(timer: StripTimer)
}
