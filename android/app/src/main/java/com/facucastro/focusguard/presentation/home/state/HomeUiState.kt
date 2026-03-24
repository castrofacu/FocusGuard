package com.facucastro.focusguard.presentation.home.state

import com.facucastro.focusguard.domain.model.DistractionEvent
import com.facucastro.focusguard.domain.model.SessionStatus

data class HomeUiState(
    val status: SessionStatus = SessionStatus.Idle,
    val elapsedSeconds: Int = 0,
    val distractionCount: Int = 0,
    val lastDistractionEvent: DistractionEvent? = null,
)