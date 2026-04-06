package com.facucastro.focusguard.domain.timer

import com.facucastro.focusguard.domain.time.TimeProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusSessionTimerFactory @Inject constructor(
    private val timeProvider: TimeProvider,
) {
    fun create(startTimeMillis: Long): FocusSessionTimer =
        FocusSessionTimer(startTimeMillis = startTimeMillis, timeProvider = timeProvider)
}
