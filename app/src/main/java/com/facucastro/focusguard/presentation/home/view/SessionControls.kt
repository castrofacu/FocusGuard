package com.facucastro.focusguard.presentation.home.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.facucastro.focusguard.domain.model.SessionStatus

@Composable
fun SessionControls(
    status: SessionStatus,
    onStartClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onResumeClicked: () -> Unit,
    onStopClicked: () -> Unit,
) {
    AnimatedContent(targetState = status, label = "session_controls") { currentStatus ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (currentStatus) {
                is SessionStatus.Idle -> {
                    Button(
                        onClick = onStartClicked,
                        modifier = Modifier.semantics {
                            contentDescription = "Start focus session"
                            role = Role.Button
                        },
                    ) {
                        Text("Start", fontSize = 16.sp)
                    }
                }

                is SessionStatus.Running -> {
                    OutlinedButton(
                        onClick = onPauseClicked,
                        modifier = Modifier.semantics {
                            contentDescription = "Pause focus session"
                            role = Role.Button
                        },
                    ) {
                        Text("Pause", fontSize = 16.sp)
                    }
                    Button(
                        onClick = onStopClicked,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                        modifier = Modifier.semantics {
                            contentDescription = "Stop and save focus session"
                            role = Role.Button
                        },
                    ) {
                        Text("Stop", fontSize = 16.sp)
                    }
                }

                is SessionStatus.Paused -> {
                    Button(
                        onClick = onResumeClicked,
                        modifier = Modifier.semantics {
                            contentDescription = "Resume focus session"
                            role = Role.Button
                        },
                    ) {
                        Text("Resume", fontSize = 16.sp)
                    }
                    OutlinedButton(
                        onClick = onStopClicked,
                        modifier = Modifier.semantics {
                            contentDescription = "Stop and save focus session"
                            role = Role.Button
                        },
                    ) {
                        Text("Stop", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}