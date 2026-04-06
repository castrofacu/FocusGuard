package com.facucastro.focusguard.providers.domain.timer

import com.facucastro.focusguard.domain.timer.FocusSessionTimer
import com.facucastro.focusguard.providers.domain.time.StepTimeProvider

fun providesFocusSessionTimer(
    startTimeMillis: Long = 0L,
    timeProvider: StepTimeProvider = StepTimeProvider(initialMillis = startTimeMillis),
): FocusSessionTimer = FocusSessionTimer(
    startTimeMillis = startTimeMillis,
    timeProvider = timeProvider,
)
