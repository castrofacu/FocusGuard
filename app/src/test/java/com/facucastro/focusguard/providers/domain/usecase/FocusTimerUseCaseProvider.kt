package com.facucastro.focusguard.providers.domain.usecase

import com.facucastro.focusguard.domain.timer.FocusSessionTimer
import com.facucastro.focusguard.domain.usecase.FocusTimerUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

fun providesFakeFocusTimerUseCase(
    invokeResult: Flow<Int> = emptyFlow(),
): FocusTimerUseCase {
    return object : FocusTimerUseCase() {
        override fun invoke(timer: FocusSessionTimer): Flow<Int> = invokeResult
    }
}
