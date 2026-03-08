package com.facucastro.focusguard.data.remote.dto

import com.facucastro.focusguard.domain.model.FocusSession

fun FocusSession.toDto() = FocusSessionDto(
    id = id,
    startTime = startTime,
    durationSeconds = durationSeconds,
    distractionCount = distractionCount
)

fun FocusSessionDto.toDomain() = FocusSession(
    id = id,
    startTime = startTime,
    durationSeconds = durationSeconds,
    distractionCount = distractionCount
)
