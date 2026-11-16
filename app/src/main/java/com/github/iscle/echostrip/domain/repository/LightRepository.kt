package com.github.iscle.echostrip.domain.repository

import com.github.iscle.echostrip.domain.model.LightState
import com.github.iscle.echostrip.domain.model.Rgb
import kotlinx.coroutines.flow.Flow

interface LightRepository {
    val lightState: Flow<LightState>

    suspend fun setPower(on: Boolean)

    suspend fun setColor(color: Rgb)

    suspend fun setBrightness(percent: Int)
}
