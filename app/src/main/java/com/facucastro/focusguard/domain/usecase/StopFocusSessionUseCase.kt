package com.facucastro.focusguard.domain.usecase

import com.facucastro.focusguard.domain.model.FocusSession
import com.facucastro.focusguard.domain.repository.FocusRepository
import javax.inject.Inject

class StopFocusSessionUseCase @Inject constructor(
    private val repository: FocusRepository
) {
    suspend operator fun invoke(session: FocusSession, distractionCount: Int) {
        val durationSeconds = ((System.currentTimeMillis() - session.startTime) / 1000).toInt()
        val completed = session.copy(
            durationSeconds = durationSeconds,
            distractionCount = distractionCount
        )
        repository.saveSession(completed)
    }
}
