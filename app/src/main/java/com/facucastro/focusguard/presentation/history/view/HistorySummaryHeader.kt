package com.facucastro.focusguard.presentation.history.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun HistorySummaryHeader(
    totalSessions: Int,
    totalFocusMinutes: Int,
    avgDistractions: Float,
    modifier: Modifier = Modifier,
) {
    val totalTimeLabel = if (totalFocusMinutes >= 60) {
        "${totalFocusMinutes / 60}h ${totalFocusMinutes % 60}m"
    } else {
        "$totalFocusMinutes min"
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatCard(
            icon = Icons.Filled.CheckCircle,
            iconDescription = "Sessions",
            value = "$totalSessions",
            label = "Sessions",
            modifier = Modifier.weight(1f),
        )
        StatCard(
            icon = Icons.Filled.Timer,
            iconDescription = "Total time",
            value = totalTimeLabel,
            label = "Total time",
            modifier = Modifier.weight(1f),
        )
        StatCard(
            icon = Icons.Filled.Warning,
            iconDescription = "Average distractions",
            value = "%.1f".format(avgDistractions),
            label = "Avg. distractions",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    iconDescription: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = iconDescription,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HistorySummaryHeaderPreview() {
    MaterialTheme {
        HistorySummaryHeader(
            totalSessions = 12,
            totalFocusMinutes = 145,
            avgDistractions = 1.5f,
            modifier = Modifier.padding(16.dp)
        )
    }
}
