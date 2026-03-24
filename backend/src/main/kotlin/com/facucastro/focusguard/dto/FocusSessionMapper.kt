package com.facucastro.focusguard.dto

import com.facucastro.focusguard.domain.FocusSession

fun FocusSession.toDto() = FocusSessionDto(
    id = id,
    startTime = startTime,
    durationSeconds = durationSeconds,
    distractionCount = distractionCount
)

fun FocusSessionDto.toEntity() = FocusSession(
    id = id,
    startTime = startTime,
    durationSeconds = durationSeconds,
    distractionCount = distractionCount
)
