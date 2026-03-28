package com.facucastro.focusguard.presentation.home.view.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

@Composable
fun TimerDisplay(elapsedSeconds: Int) {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    Text(
        text = "%02d:%02d".format(minutes, seconds),
        style = MaterialTheme.typography.displayLarge.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
        ),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.semantics {
            contentDescription = "Elapsed time: $minutes minutes $seconds seconds"
        },
    )
}
