package com.facucastro.focusguard.domain.usecase

import com.facucastro.focusguard.domain.time.TimeProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

open class FocusTimerUseCase @Inject constructor(
    private val timeProvider: TimeProvider,
) {
    open operator fun invoke(startTimeMillis: Long): Flow<Int> = flow {
        while (true) {
            delay(500L)
            val elapsed = (timeProvider.getCurrentTimeMillis() - startTimeMillis) / 1000
            emit(elapsed.toInt())
        }
    }
}
