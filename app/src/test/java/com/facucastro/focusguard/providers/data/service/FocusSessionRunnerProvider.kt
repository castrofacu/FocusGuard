package com.facucastro.focusguard.providers.data.service

import com.facucastro.focusguard.data.service.FocusSessionRunner
import com.facucastro.focusguard.domain.model.DistractionEvent
import com.facucastro.focusguard.domain.usecase.ObserveDistractionsUseCase
import com.facucastro.focusguard.domain.usecase.StartFocusSessionUseCase
import com.facucastro.focusguard.domain.usecase.StopFocusSessionUseCase
import com.facucastro.focusguard.providers.domain.notification.providesNotifierMock
import com.facucastro.focusguard.providers.domain.repository.providesMockFocusRepository
import com.facucastro.focusguard.providers.domain.sensor.FakeDistractionMonitor
import com.facucastro.focusguard.providers.domain.sensor.providesFakeDistractionMonitor
import com.facucastro.focusguard.providers.domain.time.providesFakeTimeProvider
import com.facucastro.focusguard.providers.domain.usecase.providesFakeFocusTimerUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

fun providesFocusSessionRunner(
    timerFlow: Flow<Int> = emptyFlow(),
    monitor: FakeDistractionMonitor = providesFakeDistractionMonitor(),
    saveResult: Result<Unit> = Result.success(Unit),
): FocusSessionRunner {
    val timeProvider = providesFakeTimeProvider(timeToReturn = 0L)
    return FocusSessionRunner(
        startFocusSessionUseCase = StartFocusSessionUseCase(timeProvider),
        stopFocusSessionUseCase = StopFocusSessionUseCase(
            repository = providesMockFocusRepository(saveResult = saveResult),
            timeProvider = timeProvider,
        ),
        focusTimerUseCase = providesFakeFocusTimerUseCase(invokeResult = timerFlow),
        observeDistractionsUseCase = ObserveDistractionsUseCase(
            distractionMonitor = monitor,
            distractionNotifier = providesNotifierMock(),
            timeProvider = timeProvider,
        ),
        timerFactory = com.facucastro.focusguard.domain.timer.FocusSessionTimerFactory(timeProvider),
    )
}
