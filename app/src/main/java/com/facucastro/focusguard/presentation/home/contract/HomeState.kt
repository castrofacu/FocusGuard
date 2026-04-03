package com.facucastro.focusguard.presentation.home.contract

import com.facucastro.focusguard.domain.model.DistractionEvent
import com.facucastro.focusguard.domain.model.SessionStatus

data class HomeState(
    val status: SessionStatus = SessionStatus.Idle,
    val elapsedSeconds: Int = 0,
    val distractionCount: Int = 0,
    val lastDistractionEvent: DistractionEvent? = null,
) {
    val shieldStrength: Int = (100 - distractionCount * 10).coerceAtLeast(0)
}
