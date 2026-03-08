package com.facucastro.focusguard.data.remote.dto

data class FocusSessionDto(
    val id: Long,
    val startTime: Long,
    val durationSeconds: Int,
    val distractionCount: Int
)
