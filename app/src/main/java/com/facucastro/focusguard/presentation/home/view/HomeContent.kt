package com.facucastro.focusguard.presentation.home.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.facucastro.focusguard.presentation.home.state.HomeUiState

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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
    ) {
        TimerDisplay(elapsedSeconds = uiState.elapsedSeconds)

        DistractionCounter(count = uiState.distractionCount)

        uiState.lastDistractionEvent?.let { DistractionEventCard(event = it) }

        SessionControls(
            status = uiState.status,
            onStartClicked = onStartClicked,
            onPauseClicked = onPauseClicked,
            onResumeClicked = onResumeClicked,
            onStopClicked = onStopClicked,
        )
    }
}