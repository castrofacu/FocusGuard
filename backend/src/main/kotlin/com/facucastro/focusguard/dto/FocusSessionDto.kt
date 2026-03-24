package com.facucastro.focusguard.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive

data class FocusSessionDto(

    @field:Positive(message = "id must be positive")
    val id: Long,

    @field:Positive(message = "startTime must be positive")
    val startTime: Long,

    @field:Min(value = 1, message = "durationSeconds must be at least 1")
    val durationSeconds: Int,

    @field:Min(value = 0, message = "distractionCount cannot be negative")
    val distractionCount: Int
)
