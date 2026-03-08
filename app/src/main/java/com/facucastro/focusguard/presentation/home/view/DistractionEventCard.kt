package com.facucastro.focusguard.presentation.home.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.facucastro.focusguard.domain.model.DistractionEvent

@Composable
fun DistractionEventCard(event: DistractionEvent) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
    ) {
        Text(
            text = when (event) {
                is DistractionEvent.Movement -> "Movement detected"
                is DistractionEvent.Noise -> "Noise detected"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(16.dp),
        )
    }
}