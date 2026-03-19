package com.facucastro.focusguard.presentation.history.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.facucastro.focusguard.presentation.core.LoadingComponent
import com.facucastro.focusguard.presentation.history.state.HistoryUiState
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryContent(uiState: HistoryUiState, modifier: Modifier = Modifier) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault()) }

    when {
        uiState.isLoading -> {
            LoadingComponent(modifier = Modifier.fillMaxSize())
        }
        uiState.sessionGroups.isEmpty() -> {
            HistoryEmptyState(modifier = modifier)
        }
        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
            ) {
                item(key = "summary_header") {
                    HistorySummaryHeader(
                        totalSessions = uiState.totalSessions,
                        totalFocusMinutes = uiState.totalFocusMinutes,
                        avgDistractions = uiState.avgDistractions,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }

                uiState.sessionGroups.forEach { group ->
                    val dateKey = when (val dl = group.dateLabel) {
                        HistoryUiState.DateLabel.Today -> "today"
                        HistoryUiState.DateLabel.Yesterday -> "yesterday"
                        is HistoryUiState.DateLabel.Other -> dl.date.toString()
                    }

                    item(key = "header_$dateKey") {
                        val label = when (val dl = group.dateLabel) {
                            HistoryUiState.DateLabel.Today -> "Today"
                            HistoryUiState.DateLabel.Yesterday -> "Yesterday"
                            is HistoryUiState.DateLabel.Other -> dl.date.format(dateFormatter)
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                        )
                    }

                    items(
                        items = group.sessions,
                        key = { it.id }
                    ) { session ->
                        HistorySessionCard(
                            session = session,
                            zoneId = uiState.zoneId,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
