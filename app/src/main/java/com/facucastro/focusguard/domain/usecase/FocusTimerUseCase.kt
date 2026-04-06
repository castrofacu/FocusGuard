package com.facucastro.focusguard.domain.usecase

import com.facucastro.focusguard.domain.timer.FocusSessionTimer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

open class FocusTimerUseCase @Inject constructor() {
    open operator fun invoke(timer: FocusSessionTimer): Flow<Int> = flow {
        var lastEmitted = -1
        while (true) {
            delay(1000L)
            if (timer.isPaused) continue
            val elapsed = timer.elapsedSeconds()
            if (elapsed != lastEmitted) {
                lastEmitted = elapsed
                emit(elapsed)
            }
        }
    }
}
