package com.facucastro.focusguard.providers.domain.model

import com.facucastro.focusguard.domain.model.FocusSession

fun providesFocusSession(
    startTime: Long,
    id: Long = 1L,
    durationSeconds: Int = 60,
    distractionCount: Int = 0
) = FocusSession(
    id = id,
    startTime = startTime,
    durationSeconds = durationSeconds,
    distractionCount = distractionCount
)