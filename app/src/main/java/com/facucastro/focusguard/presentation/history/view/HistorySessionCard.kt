package com.facucastro.focusguard.presentation.history.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.facucastro.focusguard.domain.model.FocusSession
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistorySessionCard(
    session: FocusSession,
    zoneId: ZoneId,
    modifier: Modifier = Modifier
) {
    val statusColor = when {
        session.distractionCount == 0 -> MaterialTheme.colorScheme.primary
        session.distractionCount <= 2 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    val timeLabel = remember(session.startTime, zoneId) {
        Instant.ofEpochMilli(session.startTime)
            .atZone(zoneId)
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    }

    val durationLabel = remember(session.durationSeconds) {
        val totalMinutes = session.durationSeconds / 60
        if (totalMinutes >= 60) {
            "${totalMinutes / 60}h ${totalMinutes % 60}m"
        } else {
            "$totalMinutes min"
        }
    }

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(statusColor),
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SessionStatInfo(
                        icon = Icons.Filled.Timer,
                        value = durationLabel,
                        label = "Duration"
                    )
                    SessionStatInfo(
                        icon = Icons.Filled.Warning,
                        value = "${session.distractionCount}",
                        label = "Distractions"
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionStatInfo(
    icon: ImageVector,
    value: String,
    label: String
) {
    Row(
        modifier = Modifier.semantics(mergeDescendants = true) {
            this.contentDescription = "$label: $value"
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
