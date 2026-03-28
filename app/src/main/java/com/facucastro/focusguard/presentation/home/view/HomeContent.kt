package com.facucastro.focusguard.presentation.home.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.facucastro.focusguard.domain.model.SessionStatus
import com.facucastro.focusguard.presentation.home.state.HomeUiState
import com.facucastro.focusguard.presentation.home.view.component.DistractionEventCard
import com.facucastro.focusguard.presentation.home.view.component.SessionControls
import com.facucastro.focusguard.presentation.home.view.component.ShieldOrb
import com.facucastro.focusguard.presentation.home.view.component.StatCard
import com.facucastro.focusguard.presentation.home.view.component.TimerDisplay

@Composable
fun HomeContent(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onStartClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onResumeClicked: () -> Unit,
    onStopClicked: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        ShieldOrb(
            isActive = uiState.status is SessionStatus.Running,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TimerDisplay(elapsedSeconds = uiState.elapsedSeconds)
            Text(
                text = when (uiState.status) {
                    is SessionStatus.Idle -> "READY TO DEFEND"
                    is SessionStatus.Running -> "DEFENDING"
                    is SessionStatus.Paused -> "SHIELD PAUSED"
                },
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                icon = Icons.Filled.Bolt,
                iconTint = MaterialTheme.colorScheme.error,
                label = "Distractions",
                value = uiState.distractionCount.toString(),
                subtitle = "Breaches today",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                icon = Icons.Filled.Shield,
                iconTint = MaterialTheme.colorScheme.secondary,
                label = "Strength",
                value = "${uiState.shieldStrength}%",
                subtitle = "Fortified status",
                modifier = Modifier.weight(1f),
            )
        }

        uiState.lastDistractionEvent?.let {
            DistractionEventCard(event = it)
        } ?: Spacer(Modifier.height(0.dp))

        SessionControls(
            status = uiState.status,
            onStartClicked = onStartClicked,
            onPauseClicked = onPauseClicked,
            onResumeClicked = onResumeClicked,
            onStopClicked = onStopClicked,
        )
    }
}