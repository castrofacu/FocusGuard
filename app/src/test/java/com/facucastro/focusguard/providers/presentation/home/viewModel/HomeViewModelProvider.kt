package com.facucastro.focusguard.providers.presentation.home.viewModel

import com.facucastro.focusguard.domain.model.DistractionEvent
import com.facucastro.focusguard.domain.notification.DistractionNotifier
import com.facucastro.focusguard.domain.sensor.DistractionMonitor
import com.facucastro.focusguard.domain.usecase.ObserveDistractionsUseCase
import com.facucastro.focusguard.domain.usecase.StartFocusSessionUseCase
import com.facucastro.focusguard.domain.usecase.StopFocusSessionUseCase
import com.facucastro.focusguard.presentation.home.viewModel.HomeViewModel
import com.facucastro.focusguard.providers.domain.repository.providesMockFocusRepository
import com.facucastro.focusguard.providers.domain.sensor.providesFakeDistractionMonitor
import com.facucastro.focusguard.providers.domain.time.StepTimeProvider
import com.facucastro.focusguard.providers.domain.usecase.providesFakeFocusTimerUseCase
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.emptyFlow

fun providesHomeViewModel(
    distractionMonitor: DistractionMonitor = providesFakeDistractionMonitor(),
    stopResult: Result<Unit> = Result.success(Unit),
): HomeViewModel {
    val timeProvider = StepTimeProvider()
    val focusRepository = providesMockFocusRepository(saveResult = stopResult)
    val distractionNotifier = mockk<DistractionNotifier> {
        every { notifyDistraction(any<DistractionEvent>()) } just runs
    }

    return HomeViewModel(
        startFocusSessionUseCase = StartFocusSessionUseCase(timeProvider),
        stopFocusSessionUseCase = StopFocusSessionUseCase(focusRepository, timeProvider),
        focusTimerUseCase = providesFakeFocusTimerUseCase(
            invokeResult = emptyFlow()
        ),
        observeDistractionsUseCase = ObserveDistractionsUseCase(
            distractionMonitor = distractionMonitor,
            distractionNotifier = distractionNotifier,
            timeProvider = timeProvider,
        ),
    )
}
