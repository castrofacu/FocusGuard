package com.facucastro.focusguard.presentation.history.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.facucastro.focusguard.presentation.home.view.component.StatCard

@Composable
fun HistorySummaryHeader(
    totalSessions: Int,
    totalFocusMinutes: Int,
    totalDistractions: Int,
    modifier: Modifier = Modifier,
) {
    val totalTimeValue = when {
        totalFocusMinutes >= 60 -> "${totalFocusMinutes / 60}h"
        else -> "${totalFocusMinutes}m"
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatCard(
            icon = Icons.Filled.AutoAwesome,
            iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            label = "Sessions",
            value = "$totalSessions",
            subtitle = "total",
            modifier = Modifier.weight(1f),
        )
        StatCard(
            icon = Icons.Filled.Timer,
            iconTint = MaterialTheme.colorScheme.secondary,
            label = "Focus",
            value = totalTimeValue,
            subtitle = "this week",
            modifier = Modifier.weight(1f),
        )
        StatCard(
            icon = Icons.Filled.Shield,
            iconTint = MaterialTheme.colorScheme.primary,
            label = "Shields",
            value = "$totalDistractions",
            subtitle = "blocked",
            modifier = Modifier.weight(1f),
        )
    }
}
