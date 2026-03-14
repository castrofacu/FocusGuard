package com.facucastro.focusguard.domain.usecase

import com.facucastro.focusguard.domain.model.FocusSession
import com.facucastro.focusguard.domain.repository.FocusRepository
import com.facucastro.focusguard.domain.time.TimeProvider
import javax.inject.Inject

class StopFocusSessionUseCase @Inject constructor(
    private val repository: FocusRepository,
    private val timeProvider: TimeProvider
) {
    suspend operator fun invoke(session: FocusSession, distractionCount: Int): Result<Unit> {
        val durationSeconds = ((timeProvider.getCurrentTimeMillis() - session.startTime) / 1000).toInt()
        val completed = session.copy(
            durationSeconds = durationSeconds,
            distractionCount = distractionCount
        )
        return repository.saveSession(completed)
    }
}
