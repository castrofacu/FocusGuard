package com.facucastro.focusguard.presentation.history.state

import com.facucastro.focusguard.domain.model.FocusSession
import java.time.LocalDate
import java.time.ZoneId

data class HistoryUiState(
    val isLoading: Boolean = true,
    val sessionGroups: List<SessionGroup> = emptyList(),
    val totalSessions: Int = 0,
    val totalFocusMinutes: Int = 0,
    val avgDistractions: Float = 0f,
    val zoneId: ZoneId = ZoneId.systemDefault()
) {
    sealed class DateLabel {
        object Today : DateLabel()
        object Yesterday : DateLabel()
        data class Other(val date: LocalDate) : DateLabel()
    }

    data class SessionGroup(
        val dateLabel: DateLabel,
        val sessions: List<FocusSession>
    )
}
