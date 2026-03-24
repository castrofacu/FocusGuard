package com.facucastro.focusguard.domain.model

data class FocusSession(
    val id: Long,
    val startTime: Long,
    val durationSeconds: Int,
    val distractionCount: Int
)
