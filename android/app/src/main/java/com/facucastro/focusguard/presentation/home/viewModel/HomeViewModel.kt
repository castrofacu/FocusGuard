package com.facucastro.focusguard.presentation.home.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facucastro.focusguard.domain.model.FocusSession
import com.facucastro.focusguard.domain.model.SessionStatus
import com.facucastro.focusguard.domain.sensor.DistractionMonitor
import com.facucastro.focusguard.domain.usecase.StartFocusSessionUseCase
import com.facucastro.focusguard.domain.usecase.StopFocusSessionUseCase
import com.facucastro.focusguard.notification.FocusNotificationManager
import com.facucastro.focusguard.presentation.home.state.HomeEvent
import com.facucastro.focusguard.presentation.home.state.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val distractionMonitor: DistractionMonitor,
    private val startFocusSessionUseCase: StartFocusSessionUseCase,
    private val stopFocusSessionUseCase: StopFocusSessionUseCase,
    private val focusNotificationManager: FocusNotificationManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events: Flow<HomeEvent> = _events.receiveAsFlow()

    private var activeSession: FocusSession? = null
    private var timerJob: Job? = null
    private var sensorJob: Job? = null
    private var notificationJob: Job? = null

    fun onStartClicked() {
        if (_uiState.value.status != SessionStatus.Idle) return
        viewModelScope.launch { _events.send(HomeEvent.RequestPermissions) }
    }

    fun onPermissionsResult(isNotificationGranted: Boolean) {
        if (!isNotificationGranted) {
            viewModelScope.launch {
                _events.send(
                    HomeEvent.NotificationsPermissionDenied
                )
            }
        }
        startSession()
    }

    private fun startSession() {
        activeSession = startFocusSessionUseCase()
        _uiState.update {
            it.copy(
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

    fun onPauseClicked() {
        timerJob?.cancel()
        sensorJob?.cancel()
        notificationJob?.cancel()
        distractionMonitor.stop()
        _uiState.update { it.copy(status = SessionStatus.Paused) }
    }

    fun onResumeClicked() {
        _uiState.update { it.copy(status = SessionStatus.Running) }
        distractionMonitor.start(viewModelScope)
        startMonitoringJobs()
        startTimer()
    }

    fun onStopClicked() {
        timerJob?.cancel()
        sensorJob?.cancel()
        notificationJob?.cancel()
        distractionMonitor.stop()

        val session = activeSession
        val count = _uiState.value.distractionCount
        if (session != null) {
            viewModelScope.launch {
                stopFocusSessionUseCase(session, count)
                    .onFailure {
                        _events.send(HomeEvent.FailedToSaveSession)
                    }
            }
        }

        activeSession = null
        _uiState.update {
            it.copy(
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
                _uiState.update {
                    it.copy(
                        distractionCount = it.distractionCount + 1,
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
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        distractionMonitor.stop()
    }
}
