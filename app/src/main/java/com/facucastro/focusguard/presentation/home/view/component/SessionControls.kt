package com.facucastro.focusguard.presentation.home.view.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.facucastro.focusguard.domain.model.SessionStatus
import com.facucastro.focusguard.presentation.core.component.FocusGuardButton
import com.facucastro.focusguard.presentation.core.component.FocusGuardButtonVariant

@Composable
fun SessionControls(
    status: SessionStatus,
    onStartClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onResumeClicked: () -> Unit,
    onStopClicked: () -> Unit,
) {
    AnimatedContent(
        targetState = status,
        label = "session_controls",
        transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { currentStatus ->
        when (currentStatus) {
            is SessionStatus.Idle -> {
                FocusGuardButton(
                    text = "Start Shield",
                    onClick = onStartClicked,
                    icon = Icons.Filled.Shield,
                    accessibilityDescription = "Start focus session",
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            is SessionStatus.Running -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FocusGuardButton(
                        text = "Pause",
                        onClick = onPauseClicked,
                        variant = FocusGuardButtonVariant.Outlined,
                        icon = Icons.Filled.Pause,
                        accessibilityDescription = "Pause focus session",
                        modifier = Modifier.weight(1f),
                    )
                    FocusGuardButton(
                        text = "Stop",
                        onClick = onStopClicked,
                        variant = FocusGuardButtonVariant.Danger,
                        icon = Icons.Filled.Stop,
                        accessibilityDescription = "Stop and save focus session",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            is SessionStatus.Paused -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FocusGuardButton(
                        text = "Resume",
                        onClick = onResumeClicked,
                        icon = Icons.Filled.PlayArrow,
                        accessibilityDescription = "Resume focus session",
                        modifier = Modifier.weight(1f),
                    )
                    FocusGuardButton(
                        text = "Stop",
                        onClick = onStopClicked,
                        variant = FocusGuardButtonVariant.Danger,
                        icon = Icons.Filled.Stop,
                        accessibilityDescription = "Stop and save focus session",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}