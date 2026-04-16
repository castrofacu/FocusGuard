package com.facucastro.focusguard.domain.model

data class FocusSessionState(
    val status: SessionStatus = SessionStatus.Idle,
    val elapsedSeconds: Int = 0,
    val distractionCount: Int = 0,
    val lastDistractionEvent: DistractionEvent? = null,
)