package com.github.iscle.echostrip.domain.usecase

import com.github.iscle.echostrip.domain.model.ConnectionState
import com.github.iscle.echostrip.domain.repository.StripRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.StateFlow

@Inject
class ObserveConnectionUseCase(private val repository: StripRepository) {
    operator fun invoke(): StateFlow<ConnectionState> = repository.connectionState
}

/** Holds the connection while the app is visible and lets go of it in the background. */
@Inject
class SetStripActiveUseCase(private val repository: StripRepository) {
    operator fun invoke(active: Boolean) {
        if (active) repository.connect() else repository.disconnect()
    }
}
