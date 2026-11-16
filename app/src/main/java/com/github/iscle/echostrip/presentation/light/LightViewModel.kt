package com.github.iscle.echostrip.presentation.light

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.iscle.echostrip.di.ViewModelKey
import com.github.iscle.echostrip.domain.model.LightState
import com.github.iscle.echostrip.domain.model.Rgb
import com.github.iscle.echostrip.domain.usecase.ObserveLightStateUseCase
import com.github.iscle.echostrip.domain.usecase.SetBrightnessUseCase
import com.github.iscle.echostrip.domain.usecase.SetColorUseCase
import com.github.iscle.echostrip.domain.usecase.SetPowerUseCase
import com.github.iscle.echostrip.presentation.common.rgbFromHue
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class, binding = binding<ViewModel>())
@ViewModelKey(LightViewModel::class)
@Inject
class LightViewModel(
    observeLightState: ObserveLightStateUseCase,
    private val setPower: SetPowerUseCase,
    private val setColor: SetColorUseCase,
    private val setBrightness: SetBrightnessUseCase,
) : ViewModel() {
    val lightState: StateFlow<LightState> = observeLightState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LightState.Default)

    fun onPowerChange(on: Boolean) {
        viewModelScope.launch { setPower(on) }
    }

    fun onColorPick(color: Rgb) {
        viewModelScope.launch { setColor(color, lightState.value) }
    }

    fun onHueChange(hue: Float) = onColorPick(rgbFromHue(hue))

    fun onBrightnessChange(percent: Int) {
        viewModelScope.launch { setBrightness(percent) }
    }
}
