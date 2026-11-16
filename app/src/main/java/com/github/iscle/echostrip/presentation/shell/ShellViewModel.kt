package com.github.iscle.echostrip.presentation.shell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.iscle.echostrip.di.ViewModelKey
import com.github.iscle.echostrip.domain.model.ConnectionState
import com.github.iscle.echostrip.domain.model.LightState
import com.github.iscle.echostrip.domain.usecase.ObserveConnectionUseCase
import com.github.iscle.echostrip.domain.usecase.ObserveLightStateUseCase
import com.github.iscle.echostrip.domain.usecase.ObserveTimersUseCase
import com.github.iscle.echostrip.domain.usecase.SetStripActiveUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime

data class ShellUiState(
    val connection: ConnectionState = ConnectionState.Disconnected,
    val light: LightState = LightState.Default,
    val next: NextEvent? = null,
    val now: LocalDateTime = LocalDateTime.now(),
)

/** State shared by the rail around every screen: the link, the strip's colour and what's next. */
@ContributesIntoMap(AppScope::class, binding = binding<ViewModel>())
@ViewModelKey(ShellViewModel::class)
@Inject
class ShellViewModel(
    observeConnection: ObserveConnectionUseCase,
    observeLightState: ObserveLightStateUseCase,
    observeTimers: ObserveTimersUseCase,
    private val setStripActive: SetStripActiveUseCase,
) : ViewModel() {
    private val minutes = flow {
        while (true) {
            emit(LocalDateTime.now())
            delay(60_000L - System.currentTimeMillis() % 60_000L)
        }
    }

    val uiState: StateFlow<ShellUiState> = combine(
        observeConnection(),
        observeLightState(),
        observeTimers(),
        minutes,
    ) { connection, light, timers, now ->
        ShellUiState(connection, light, nextEvent(timers, now), now)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShellUiState())

    fun onVisibleChange(visible: Boolean) = setStripActive(visible)
}
