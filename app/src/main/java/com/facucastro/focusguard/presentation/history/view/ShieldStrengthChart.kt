package com.facucastro.focusguard.presentation.history.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val ChartHeight = 100.dp
private val BarWidth = 16.dp

@Composable
fun ShieldStrengthChart(
    weeklyMinutesByDay: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
) {
    val maxMinutes = weeklyMinutesByDay.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1
    val todayIndex = weeklyMinutesByDay.size - 1
    val isPeakGuard = todayIndex >= 0
            && weeklyMinutesByDay[todayIndex].second > 0
            && weeklyMinutesByDay[todayIndex].second == maxMinutes

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Shield Strength",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (isPeakGuard) {
                    Text(
                        text = "PEAK GUARD",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                RoundedCornerShape(8.dp),
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                weeklyMinutesByDay.forEachIndexed { index, (day, minutes) ->
                    val isToday = index == todayIndex
                    val barColor = if (isToday) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondary
                    val dayColor = if (isToday) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                    val fraction = minutes.toFloat() / maxMinutes
                    val barHeight = if (minutes > 0) {
                        (ChartHeight * fraction).coerceAtLeast(4.dp)
                    } else {
                        0.dp
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .height(ChartHeight + 20.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .width(BarWidth)
                                .height(barHeight)
                                .background(barColor, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            color = dayColor,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
