package com.facucastro.focusguard.data.local.db

import com.facucastro.focusguard.domain.model.FocusSession

fun SessionEntity.toDomain(): FocusSession = FocusSession(
    id = id,
    startTime = startTime,
    durationSeconds = durationSeconds,
    distractionCount = distractionCount
)

fun FocusSession.toEntity(isSynced: Boolean = false): SessionEntity = SessionEntity(
    id = id,
    startTime = startTime,
    durationSeconds = durationSeconds,
    distractionCount = distractionCount,
    isSynced = isSynced
)
