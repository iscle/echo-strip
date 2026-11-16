package com.github.iscle.echostrip.domain.usecase

import com.github.iscle.echostrip.domain.model.LightState
import com.github.iscle.echostrip.domain.model.Rgb
import com.github.iscle.echostrip.domain.repository.LightRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

@Inject
class ObserveLightStateUseCase(private val repository: LightRepository) {
    operator fun invoke(): Flow<LightState> = repository.lightState
}

@Inject
class SetPowerUseCase(private val repository: LightRepository) {
    suspend operator fun invoke(on: Boolean) = repository.setPower(on)
}

/** Picking a colour while the strip is off also turns it on, as people expect from a lamp. */
@Inject
class SetColorUseCase(private val repository: LightRepository) {
    suspend operator fun invoke(color: Rgb, current: LightState) {
        if (!current.isOn) repository.setPower(true)
        repository.setColor(color)
    }
}

@Inject
class SetBrightnessUseCase(private val repository: LightRepository) {
    suspend operator fun invoke(percent: Int) = repository.setBrightness(percent.coerceIn(1, 100))
}
