package com.facucastro.focusguard.presentation.home.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@Composable
fun DistractionCounter(count: Int) {
    Text(
        text = "Distractions: $count",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.semantics {
            contentDescription = "Number of detected distractions: $count"
        },
    )
}