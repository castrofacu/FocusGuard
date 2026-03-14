package com.facucastro.focusguard.domain.usecase

import com.facucastro.focusguard.domain.time.TimeProvider
import com.facucastro.focusguard.domain.model.FocusSession
import javax.inject.Inject

class StartFocusSessionUseCase @Inject constructor(
    private val timeProvider: TimeProvider
) {
    operator fun invoke(): FocusSession {
        val now = timeProvider.getCurrentTimeMillis()
        return FocusSession(
            id = now,
            startTime = now,
            durationSeconds = 0,
            distractionCount = 0
        )
    }
}
