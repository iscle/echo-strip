package com.github.iscle.echostrip.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.iscle.echostrip.di.ViewModelKey
import com.github.iscle.echostrip.domain.model.StripTimer
import com.github.iscle.echostrip.domain.usecase.ObserveTimersUseCase
import com.github.iscle.echostrip.domain.usecase.SaveTimerUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek

@ContributesIntoMap(AppScope::class, binding = binding<ViewModel>())
@ViewModelKey(ScheduleViewModel::class)
@Inject
class ScheduleViewModel(
    observeTimers: ObserveTimersUseCase,
    private val saveTimer: SaveTimerUseCase,
) : ViewModel() {
    val timers: StateFlow<List<StripTimer>> = observeTimers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StripTimer.Defaults)

    fun onEnabledChange(timer: StripTimer, enabled: Boolean) = save(timer.copy(isEnabled = enabled))

    fun onDayToggle(timer: StripTimer, day: DayOfWeek) =
        save(timer.copy(days = if (day in timer.days) timer.days - day else timer.days + day))

    /** Setting a time is a clear sign the timer should run, so it also switches it on. */
    fun onTimeChange(timer: StripTimer, hour: Int, minute: Int) =
        save(timer.copy(hour = hour, minute = minute, isEnabled = true))

    private fun save(timer: StripTimer) {
        viewModelScope.launch { saveTimer(timer) }
    }
}
