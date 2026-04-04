package com.facucastro.focusguard.presentation.history.contract

import com.facucastro.focusguard.domain.model.FocusSession
import java.time.LocalDate
import java.time.ZoneId

data class HistoryState(
    val isLoading: Boolean = true,
    val sessionGroups: List<SessionGroup> = emptyList(),
    val totalSessions: Int = 0,
    val totalFocusMinutes: Int = 0,
    val avgDistractions: Float = 0f,
    val totalDistractions: Int = 0,
    val weeklyMinutesByDay: List<Pair<String, Int>> = emptyList(),
    val zoneId: ZoneId = ZoneId.systemDefault()
)

sealed class DateLabel {
    data object Today : DateLabel()
    data object Yesterday : DateLabel()
    data class Other(val date: LocalDate) : DateLabel()
}

data class SessionGroup(
    val dateLabel: DateLabel,
    val sessions: List<FocusSession>
)
