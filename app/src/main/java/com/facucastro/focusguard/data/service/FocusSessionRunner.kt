package com.facucastro.focusguard.data.service

import android.util.Log
import com.facucastro.focusguard.domain.model.FocusSession
import com.facucastro.focusguard.domain.model.SessionStatus
import com.facucastro.focusguard.domain.session.FocusSessionController
import com.facucastro.focusguard.domain.model.FocusSessionState
import com.facucastro.focusguard.domain.timer.FocusSessionTimer
import com.facucastro.focusguard.domain.timer.FocusSessionTimerFactory
import com.facucastro.focusguard.domain.usecase.FocusTimerUseCase
import com.facucastro.focusguard.domain.usecase.ObserveDistractionsUseCase
import com.facucastro.focusguard.domain.usecase.StartFocusSessionUseCase
import com.facucastro.focusguard.domain.usecase.StopFocusSessionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FocusSessionRunner"

@Singleton
class FocusSessionRunner @Inject constructor(
    private val startFocusSessionUseCase: StartFocusSessionUseCase,
    private val stopFocusSessionUseCase: StopFocusSessionUseCase,
    private val focusTimerUseCase: FocusTimerUseCase,
    private val observeDistractionsUseCase: ObserveDistractionsUseCase,
    private val timerFactory: FocusSessionTimerFactory,
) : FocusSessionController {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(FocusSessionState())
    override val state: StateFlow<FocusSessionState> = _state.asStateFlow()

    private var activeSession: FocusSession? = null
    private var sessionTimer: FocusSessionTimer? = null
    private var timerJob: Job? = null
    private var monitorJob: Job? = null

    override fun start() {
        if (_state.value.status != SessionStatus.Idle) {
            Log.d(TAG, "start() ignored — session already active")
            return
        }

        activeSession = startFocusSessionUseCase()
        sessionTimer = timerFactory.create(
            startTimeMillis = requireNotNull(activeSession).startTime,
        )

        _state.update {
            it.copy(
                status = SessionStatus.Running,
                elapsedSeconds = 0,
                distractionCount = 0,
                lastDistractionEvent = null,
            )
        }

        scope.launch { startMonitorJob() }
        startTimerJob(requireNotNull(sessionTimer))

        Log.i(TAG, "Session started")
    }

    override fun pause() {
        if (_state.value.status != SessionStatus.Running) return
        sessionTimer?.pause()
        monitorJob?.cancel()
        _state.update { it.copy(status = SessionStatus.Paused, lastDistractionEvent = null) }
        Log.i(TAG, "Session paused")
    }

    override fun resume() {
        if (_state.value.status != SessionStatus.Paused) return
        sessionTimer?.resume()
        _state.update { it.copy(status = SessionStatus.Running) }
        scope.launch { startMonitorJob() }
        Log.i(TAG, "Session resumed")
    }

    override fun stop() {
        timerJob?.cancel()
        monitorJob?.cancel()

        val session = activeSession
        val count = _state.value.distractionCount
        if (session != null) {
            scope.launch {
                stopFocusSessionUseCase(session, count)
                    .onFailure { Log.e(TAG, "Failed to save session", it) }
            }
        }

        sessionTimer = null
        activeSession = null

        _state.update {
            it.copy(
                status = SessionStatus.Idle,
                elapsedSeconds = 0,
                distractionCount = 0,
                lastDistractionEvent = null,
            )
        }

        Log.i(TAG, "Session stopped")
    }

    private suspend fun startMonitorJob() {
        monitorJob?.cancelAndJoin()
        monitorJob = scope.launch {
            observeDistractionsUseCase().collect { event ->
                _state.update {
                    it.copy(
                        distractionCount = it.distractionCount + 1,
                        lastDistractionEvent = event,
                    )
                }
            }
        }
    }

    private fun startTimerJob(timer: FocusSessionTimer) {
        timerJob?.cancel()
        timerJob = scope.launch {
            focusTimerUseCase(timer).collect { elapsed ->
                _state.update { it.copy(elapsedSeconds = elapsed) }
            }
        }
    }
}
