package com.facucastro.focusguard.domain.session

import com.facucastro.focusguard.domain.model.DistractionEvent
import com.facucastro.focusguard.domain.model.SessionStatus

data class FocusSessionState(
    val status: SessionStatus = SessionStatus.Idle,
    val elapsedSeconds: Int = 0,
    val distractionCount: Int = 0,
    val lastDistractionEvent: DistractionEvent? = null,
)
