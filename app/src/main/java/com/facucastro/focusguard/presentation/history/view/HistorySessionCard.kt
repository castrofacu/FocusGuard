package com.facucastro.focusguard.presentation.history.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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
    modifier: Modifier = Modifier,
) {
    val (badgeText, badgeColor) = when {
        session.distractionCount == 0 -> "PERFECT GUARD" to MaterialTheme.colorScheme.secondary
        session.distractionCount <= 2 -> "DEFENDED" to MaterialTheme.colorScheme.primary
        else -> "BREACHED" to MaterialTheme.colorScheme.error
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

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription =
                    "$timeLabel, $durationLabel, ${session.distractionCount} distractions"
            },
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = durationLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "${session.distractionCount} distractions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
