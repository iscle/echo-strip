package com.github.iscle.echostrip.data.repository

import com.github.iscle.echostrip.data.ble.ElkBledomProtocol
import com.github.iscle.echostrip.data.ble.StripLink
import com.github.iscle.echostrip.data.local.LightStateDao
import com.github.iscle.echostrip.data.local.toDomain
import com.github.iscle.echostrip.data.local.toEntity
import com.github.iscle.echostrip.domain.model.LightState
import com.github.iscle.echostrip.domain.model.Rgb
import com.github.iscle.echostrip.domain.repository.LightRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class LightRepositoryImpl(
    private val dao: LightStateDao,
    private val link: StripLink,
) : LightRepository {
    private val mutex = Mutex()

    override val lightState: Flow<LightState> = dao.observe().map { it?.toDomain() ?: LightState.Default }

    override suspend fun setPower(on: Boolean) {
        update { it.copy(isOn = on) }
        link.send(ElkBledomProtocol.power(on))
    }

    override suspend fun setColor(color: Rgb) {
        update { it.copy(color = color) }
        link.send(ElkBledomProtocol.color(color))
    }

    override suspend fun setBrightness(percent: Int) {
        update { it.copy(brightness = percent) }
        link.send(ElkBledomProtocol.brightness(percent))
    }

    private suspend fun update(change: (LightState) -> LightState) = mutex.withLock {
        val current = dao.get()?.toDomain() ?: LightState.Default
        dao.upsert(change(current).toEntity())
    }
}
