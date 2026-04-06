package com.facucastro.focusguard.presentation.home.viewModel

import androidx.lifecycle.viewModelScope
import com.facucastro.focusguard.domain.model.FocusSession
import com.facucastro.focusguard.domain.model.SessionStatus
import com.facucastro.focusguard.domain.timer.FocusSessionTimer
import com.facucastro.focusguard.domain.timer.FocusSessionTimerFactory
import com.facucastro.focusguard.domain.usecase.FocusTimerUseCase
import com.facucastro.focusguard.domain.usecase.ObserveDistractionsUseCase
import com.facucastro.focusguard.domain.usecase.StartFocusSessionUseCase
import com.facucastro.focusguard.domain.usecase.StopFocusSessionUseCase
import com.facucastro.focusguard.presentation.core.viewmodel.BaseMviViewModel
import com.facucastro.focusguard.presentation.home.contract.HomeEffect
import com.facucastro.focusguard.presentation.home.contract.HomeIntent
import com.facucastro.focusguard.presentation.home.contract.HomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val startFocusSessionUseCase: StartFocusSessionUseCase,
    private val stopFocusSessionUseCase: StopFocusSessionUseCase,
    private val focusTimerUseCase: FocusTimerUseCase,
    private val observeDistractionsUseCase: ObserveDistractionsUseCase,
    private val timerFactory: FocusSessionTimerFactory,
) : BaseMviViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {

    private var activeSession: FocusSession? = null
    private var sessionTimer: FocusSessionTimer? = null
    private var timerJob: Job? = null
    private var monitorJob: Job? = null

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
        if (state.value.status != SessionStatus.Idle) return
        if (!isNotificationGranted) {
            launchEffect(HomeEffect.NotificationsPermissionDenied)
        }
        startSession()
    }

    private fun startSession() {
        activeSession = startFocusSessionUseCase()
        sessionTimer = timerFactory.create(startTimeMillis = requireNotNull(activeSession).startTime)
        setState {
            copy(
                status = SessionStatus.Running,
                elapsedSeconds = 0,
                distractionCount = 0,
                lastDistractionEvent = null,
            )
        }

        viewModelScope.launch {
            startMonitorJob()
        }
        startTimerJob(requireNotNull(sessionTimer))
    }

    private fun onPauseClicked() {
        sessionTimer?.pause()
        monitorJob?.cancel()
        setState { copy(status = SessionStatus.Paused, lastDistractionEvent = null) }
    }

    private fun onResumeClicked() {
        if (state.value.status != SessionStatus.Paused) return
        sessionTimer?.resume()
        setState { copy(status = SessionStatus.Running) }
        viewModelScope.launch {
            startMonitorJob()
        }
    }

    private fun onStopClicked() {
        timerJob?.cancel()
        monitorJob?.cancel()
        sessionTimer = null

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

    private suspend fun startMonitorJob() {
        monitorJob?.cancelAndJoin()
        monitorJob = viewModelScope.launch {
            observeDistractionsUseCase().collect { event ->
                setState {
                    copy(
                        distractionCount = distractionCount + 1,
                        lastDistractionEvent = event,
                    )
                }
            }
        }
    }

    private fun startTimerJob(timer: FocusSessionTimer) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            focusTimerUseCase(timer).collect { elapsed ->
                setState { copy(elapsedSeconds = elapsed) }
            }
        }
    }
}
