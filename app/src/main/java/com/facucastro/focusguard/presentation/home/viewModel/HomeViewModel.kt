package com.facucastro.focusguard.presentation.home.viewModel

import com.facucastro.focusguard.domain.model.FocusSession
import com.facucastro.focusguard.domain.model.SessionStatus
import com.facucastro.focusguard.domain.sensor.DistractionMonitor
import com.facucastro.focusguard.domain.usecase.StartFocusSessionUseCase
import com.facucastro.focusguard.domain.usecase.StopFocusSessionUseCase
import com.facucastro.focusguard.notification.FocusNotificationManager
import androidx.lifecycle.viewModelScope
import com.facucastro.focusguard.presentation.core.viewmodel.BaseMviViewModel
import com.facucastro.focusguard.presentation.home.contract.HomeEffect
import com.facucastro.focusguard.presentation.home.contract.HomeIntent
import com.facucastro.focusguard.presentation.home.contract.HomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val distractionMonitor: DistractionMonitor,
    private val startFocusSessionUseCase: StartFocusSessionUseCase,
    private val stopFocusSessionUseCase: StopFocusSessionUseCase,
    private val focusNotificationManager: FocusNotificationManager,
) : BaseMviViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {

    private var activeSession: FocusSession? = null
    private var timerJob: Job? = null
    private var sensorJob: Job? = null
    private var notificationJob: Job? = null

    override fun handleIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.StartClicked -> onStartClicked()
            is HomeIntent.PermissionsResult -> onPermissionsResult(intent.isNotificationGranted)
            HomeIntent.PauseClicked -> onPauseClicked()
            HomeIntent.ResumeClicked -> onResumeClicked()
            HomeIntent.StopClicked -> onStopClicked()
        }
    }

    private fun onStartClicked() {
        if (state.value.status != SessionStatus.Idle) return
        launchEffect(HomeEffect.RequestPermissions)
    }

    private fun onPermissionsResult(isNotificationGranted: Boolean) {
        if (!isNotificationGranted) {
            launchEffect(HomeEffect.NotificationsPermissionDenied)
        }
        startSession()
    }

    private fun startSession() {
        activeSession = startFocusSessionUseCase()
        setState {
            copy(
                status = SessionStatus.Running,
                elapsedSeconds = 0,
                distractionCount = 0,
                lastDistractionEvent = null,
            )
        }

        distractionMonitor.start(viewModelScope)
        startMonitoringJobs()
        startTimer()
    }

    private fun onPauseClicked() {
        timerJob?.cancel()
        sensorJob?.cancel()
        notificationJob?.cancel()
        distractionMonitor.stop()
        setState { copy(status = SessionStatus.Paused, lastDistractionEvent = null) }
    }

    private fun onResumeClicked() {
        setState { copy(status = SessionStatus.Running) }
        distractionMonitor.start(viewModelScope)
        startMonitoringJobs()
        startTimer()
    }

    private fun onStopClicked() {
        timerJob?.cancel()
        sensorJob?.cancel()
        notificationJob?.cancel()
        distractionMonitor.stop()

        val session = activeSession
        val count = state.value.distractionCount
        if (session != null) {
            viewModelScope.launch {
                stopFocusSessionUseCase(session, count)
                    .onFailure {
                        sendEffect(HomeEffect.FailedToSaveSession)
                    }
            }
        }

        activeSession = null
        setState {
            copy(
                status = SessionStatus.Idle,
                elapsedSeconds = 0,
                distractionCount = 0,
                lastDistractionEvent = null,
            )
        }
    }

    private fun startMonitoringJobs() {
        sensorJob = viewModelScope.launch {
            distractionMonitor.events.collect { event ->
                setState {
                    copy(
                        distractionCount = distractionCount + 1,
                        lastDistractionEvent = event,
                    )
                }
            }
        }

        notificationJob = viewModelScope.launch {
            var lastNotifiedAt = 0L
            distractionMonitor.events.collect { event ->
                val now = System.currentTimeMillis()
                if (now - lastNotifiedAt >= 2_000L) {
                    lastNotifiedAt = now
                    focusNotificationManager.notifyDistraction(event)
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000L)
                setState { copy(elapsedSeconds = elapsedSeconds + 1) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        distractionMonitor.stop()
    }
}
