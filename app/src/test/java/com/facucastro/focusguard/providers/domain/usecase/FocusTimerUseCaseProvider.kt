package com.facucastro.focusguard.providers.domain.usecase

import com.facucastro.focusguard.domain.usecase.FocusTimerUseCase
import com.facucastro.focusguard.providers.domain.time.providesFakeTimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

fun providesFakeFocusTimerUseCase(
    invokeResult: Flow<Int> = emptyFlow(),
): FocusTimerUseCase {
    return object : FocusTimerUseCase(providesFakeTimeProvider()) {
        override fun invoke(startTimeMillis: Long): Flow<Int> = invokeResult
    }
}