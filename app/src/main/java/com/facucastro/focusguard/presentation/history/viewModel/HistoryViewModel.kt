package com.facucastro.focusguard.presentation.history.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facucastro.focusguard.domain.model.FocusSession
import com.facucastro.focusguard.domain.time.TimeProvider
import com.facucastro.focusguard.domain.usecase.GetHistoryUseCase
import com.facucastro.focusguard.presentation.history.contract.DateLabel
import com.facucastro.focusguard.presentation.history.contract.HistoryState
import com.facucastro.focusguard.presentation.history.contract.SessionGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getHistoryUseCase: GetHistoryUseCase,
    private val timeProvider: TimeProvider
) : ViewModel() {

    val uiState: StateFlow<HistoryState> = getHistoryUseCase()
        .map { sessions -> computeUiState(sessions) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryState(
                isLoading = true,
                zoneId = timeProvider.getZoneId()
            )
        )

    private fun computeUiState(sessions: List<FocusSession>): HistoryState {
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
                    today -> DateLabel.Today
                    yesterday -> DateLabel.Yesterday
                    else -> DateLabel.Other(date)
                }
                SessionGroup(
                    dateLabel = label,
                    sessions = daySessions.sortedByDescending { it.startTime }
                )
            }

        val totalFocusMinutes = sessions.sumOf { it.durationSeconds } / 60
        val avgDistractions = if (sessions.isEmpty()) 0f
            else sessions.sumOf { it.distractionCount } / sessions.size.toFloat()
        val totalDistractions = sessions.sumOf { it.distractionCount }

        val secondsByDate = sessions.groupingBy { session ->
            Instant.ofEpochMilli(session.startTime).atZone(zone).toLocalDate()
        }.fold(0) { acc, session -> acc + session.durationSeconds }

        val weeklyMinutesByDay = (6 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val dayAbbr = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).uppercase()
            val minutes = (secondsByDate[date] ?: 0) / 60
            Pair(dayAbbr, minutes)
        }

        return HistoryState(
            isLoading = false,
            sessionGroups = groups,
            totalSessions = sessions.size,
            totalFocusMinutes = totalFocusMinutes,
            avgDistractions = avgDistractions,
            totalDistractions = totalDistractions,
            weeklyMinutesByDay = weeklyMinutesByDay,
            zoneId = zone
        )
    }
}
