package com.github.iscle.echostrip.domain.usecase

import com.github.iscle.echostrip.domain.model.StripTimer
import com.github.iscle.echostrip.domain.repository.ScheduleRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

@Inject
class ObserveTimersUseCase(private val repository: ScheduleRepository) {
    operator fun invoke(): Flow<List<StripTimer>> = repository.timers
}

@Inject
class SaveTimerUseCase(private val repository: ScheduleRepository) {
    suspend operator fun invoke(timer: StripTimer) = repository.saveTimer(timer)
}
