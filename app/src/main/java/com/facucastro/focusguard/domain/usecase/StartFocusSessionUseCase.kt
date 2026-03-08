package com.facucastro.focusguard.domain.usecase

import com.facucastro.focusguard.domain.model.FocusSession
import javax.inject.Inject

class StartFocusSessionUseCase @Inject constructor() {
    operator fun invoke(): FocusSession {
        val now = System.currentTimeMillis()
        return FocusSession(
            id = now,
            startTime = now,
            durationSeconds = 0,
            distractionCount = 0
        )
    }
}
