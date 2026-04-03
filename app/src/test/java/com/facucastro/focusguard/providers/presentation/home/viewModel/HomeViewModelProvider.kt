package com.facucastro.focusguard.providers.presentation.home.viewModel

import com.facucastro.focusguard.domain.sensor.DistractionMonitor
import com.facucastro.focusguard.domain.usecase.StartFocusSessionUseCase
import com.facucastro.focusguard.domain.usecase.StopFocusSessionUseCase
import com.facucastro.focusguard.notification.FocusNotificationManager
import com.facucastro.focusguard.presentation.home.viewModel.HomeViewModel
import com.facucastro.focusguard.providers.domain.repository.providesMockFocusRepository
import com.facucastro.focusguard.providers.domain.sensor.providesFakeDistractionMonitor
import com.facucastro.focusguard.providers.domain.time.providesFakeTimeProvider
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs

fun providesHomeViewModel(
    distractionMonitor: DistractionMonitor = providesFakeDistractionMonitor(),
    stopResult: Result<Unit> = Result.success(Unit),
): HomeViewModel {
    val timeProvider = providesFakeTimeProvider()
    val focusRepository = providesMockFocusRepository(saveResult = stopResult)
    val focusNotificationManager = mockk<FocusNotificationManager> {
        every { notifyDistraction(any()) } just runs
    }

    return HomeViewModel(
        distractionMonitor = distractionMonitor,
        startFocusSessionUseCase = StartFocusSessionUseCase(timeProvider),
        stopFocusSessionUseCase = StopFocusSessionUseCase(focusRepository, timeProvider),
        focusNotificationManager = focusNotificationManager,
    )
}
