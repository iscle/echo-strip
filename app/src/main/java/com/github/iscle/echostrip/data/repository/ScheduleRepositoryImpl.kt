package com.github.iscle.echostrip.data.repository

import com.github.iscle.echostrip.data.ble.ElkBledomProtocol
import com.github.iscle.echostrip.data.ble.StripLink
import com.github.iscle.echostrip.data.local.TimerDao
import com.github.iscle.echostrip.data.local.toDomain
import com.github.iscle.echostrip.data.local.toEntity
import com.github.iscle.echostrip.domain.model.StripTimer
import com.github.iscle.echostrip.domain.repository.ScheduleRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class ScheduleRepositoryImpl(
    private val dao: TimerDao,
    private val link: StripLink,
) : ScheduleRepository {
    override val timers: Flow<List<StripTimer>> = dao.observeAll().map { rows ->
        val saved = rows.map { it.toDomain() }
        StripTimer.Defaults.map { default -> saved.firstOrNull { it.action == default.action } ?: default }
    }

    override suspend fun saveTimer(timer: StripTimer) {
        dao.upsert(timer.toEntity())
        // The timer runs on the strip's clock, so make sure it is right before relying on it.
        link.send(ElkBledomProtocol.clock(LocalDateTime.now()))
        link.send(ElkBledomProtocol.timer(timer))
    }
}
