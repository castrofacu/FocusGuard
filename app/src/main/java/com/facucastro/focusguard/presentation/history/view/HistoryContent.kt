package com.facucastro.focusguard.presentation.history.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.facucastro.focusguard.presentation.core.component.LoadingComponent
import com.facucastro.focusguard.presentation.history.contract.DateLabel
import com.facucastro.focusguard.presentation.history.contract.HistoryState
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryContent(state: HistoryState, modifier: Modifier = Modifier) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault()) }

    when {
        state.isLoading -> {
            LoadingComponent(modifier = Modifier.fillMaxSize())
        }
        state.sessionGroups.isEmpty() -> {
            HistoryEmptyState(modifier = modifier)
        }
        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
            ) {
                item(key = "title") {
                    Text(
                        text = "Guardian Statistics",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Your complete guardian record.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(20.dp))
                }

                item(key = "summary_header") {
                    HistorySummaryHeader(
                        totalSessions = state.totalSessions,
                        totalFocusMinutes = state.totalFocusMinutes,
                        totalDistractions = state.totalDistractions,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }

                item(key = "chart") {
                    ShieldStrengthChart(
                        weeklyMinutesByDay = state.weeklyMinutesByDay,
                        modifier = Modifier.padding(bottom = 20.dp),
                    )
                }

                state.sessionGroups.forEach { group ->
                    val dateKey = when (val dl = group.dateLabel) {
                        DateLabel.Today -> "today"
                        DateLabel.Yesterday -> "yesterday"
                        is DateLabel.Other -> dl.date.toString()
                    }

                    item(key = "header_$dateKey") {
                        val label = when (val dl = group.dateLabel) {
                            DateLabel.Today -> "Today"
                            DateLabel.Yesterday -> "Yesterday"
                            is DateLabel.Other -> dl.date.format(dateFormatter)
                        }
                        Text(
                            text = label.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                        )
                    }

                    items(
                        items = group.sessions,
                        key = { it.id }
                    ) { session ->
                        HistorySessionCard(
                            session = session,
                            zoneId = state.zoneId,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
