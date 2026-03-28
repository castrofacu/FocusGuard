package com.facucastro.focusguard.presentation.home.view.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ShieldOrb(
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val durationMillis = if (isActive) 4000 else 12000
    val angle = remember { Animatable(0f) }

    LaunchedEffect(isActive) {
        while (true) {
            val remaining = 360f - angle.value
            val remainingDuration = ((remaining / 360f) * durationMillis).toInt().coerceAtLeast(1)
            angle.animateTo(
                targetValue = 360f,
                animationSpec = tween(durationMillis = remainingDuration, easing = LinearEasing),
            )
            angle.snapTo(0f)
        }
    }

    val shieldColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }
    val dot1Color  = MaterialTheme.colorScheme.secondary
    val dot2Color  = MaterialTheme.colorScheme.primary
    val ringColor  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    val innerColor = MaterialTheme.colorScheme.surface

    val statusDescription = if (isActive) "Shield active" else "Shield inactive"

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(220.dp)
            .semantics { contentDescription = statusDescription },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val orbitRadius = size.minDimension / 2f * 0.88f

            drawShieldBackground(orbitRadius, ringColor, innerColor)

            drawOrbitingDot(
                angleDegrees = angle.value,
                orbitRadius = orbitRadius,
                dotRadius = 6.dp.toPx(),
                color = dot1Color
            )

            drawOrbitingDot(
                angleDegrees = angle.value + 180f,
                orbitRadius = orbitRadius,
                dotRadius = 4.dp.toPx(),
                color = dot2Color
            )
        }

        Icon(
            imageVector = Icons.Filled.Shield,
            contentDescription = "Shield status",
            tint = shieldColor,
            modifier = Modifier.size(72.dp),
        )
    }
}

private fun DrawScope.drawShieldBackground(
    radius: Float,
    ringColor: Color,
    innerColor: Color
) {
    drawCircle(
        color = ringColor,
        radius = radius,
        style = Stroke(width = 2.dp.toPx()),
    )
    drawCircle(
        color = innerColor,
        radius = radius * 0.82f,
    )
}

private fun DrawScope.drawOrbitingDot(
    angleDegrees: Float,
    orbitRadius: Float,
    dotRadius: Float,
    color: Color
) {
    val radians = angleDegrees * (PI.toFloat() / 180f)
    drawCircle(
        color = color,
        radius = dotRadius,
        center = Offset(
            x = center.x + orbitRadius * cos(radians),
            y = center.y + orbitRadius * sin(radians),
        ),
    )
}
