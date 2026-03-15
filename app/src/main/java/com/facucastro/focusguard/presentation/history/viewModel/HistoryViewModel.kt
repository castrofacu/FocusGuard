package com.facucastro.focusguard.presentation.history.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facucastro.focusguard.domain.model.FocusSession
import com.facucastro.focusguard.domain.time.TimeProvider
import com.facucastro.focusguard.domain.usecase.GetHistoryUseCase
import com.facucastro.focusguard.presentation.history.state.HistoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getHistoryUseCase: GetHistoryUseCase,
    private val timeProvider: TimeProvider
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = getHistoryUseCase()
        .map { sessions -> computeUiState(sessions) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState(
                isLoading = true,
                zoneId = timeProvider.getZoneId()
            )
        )

    private fun computeUiState(sessions: List<FocusSession>): HistoryUiState {
        val zone = timeProvider.getZoneId()
        val today = Instant.ofEpochMilli(timeProvider.getCurrentTimeMillis())
            .atZone(zone).toLocalDate()
        val yesterday = today.minusDays(1)

        val groups = sessions
            .groupBy { session ->
                Instant.ofEpochMilli(session.startTime).atZone(zone).toLocalDate()
            }
            .entries
            .sortedByDescending { it.key }
            .map { (date, daySessions) ->
                val label = when (date) {
                    today -> HistoryUiState.DateLabel.Today
                    yesterday -> HistoryUiState.DateLabel.Yesterday
                    else -> HistoryUiState.DateLabel.Other(date)
                }
                HistoryUiState.SessionGroup(
                    dateLabel = label,
                    sessions = daySessions.sortedByDescending { it.startTime }
                )
            }

        val totalFocusMinutes = sessions.sumOf { it.durationSeconds } / 60
        val avgDistractions = if (sessions.isEmpty()) 0f
            else sessions.sumOf { it.distractionCount } / sessions.size.toFloat()

        return HistoryUiState(
            isLoading = false,
            sessionGroups = groups,
            totalSessions = sessions.size,
            totalFocusMinutes = totalFocusMinutes,
            avgDistractions = avgDistractions,
            zoneId = zone
        )
    }
}
