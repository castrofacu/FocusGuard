package com.facucastro.focusguard.presentation.core

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class FocusGuardButtonVariant { Primary, Outlined, Danger }

private val PillShape = RoundedCornerShape(50)

@Composable
fun FocusGuardButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: FocusGuardButtonVariant = FocusGuardButtonVariant.Primary,
    icon: ImageVector? = null,
    accessibilityDescription: String = text,
    enabled: Boolean = true,
) {
    val semanticsModifier = modifier
        .height(52.dp)
        .semantics {
            contentDescription = accessibilityDescription
            role = Role.Button
        }

    when (variant) {
        FocusGuardButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                shape = PillShape,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = semanticsModifier,
            ) {
                ButtonContent(icon = icon, text = text)
            }
        }

        FocusGuardButtonVariant.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                shape = PillShape,
                enabled = enabled,
                modifier = semanticsModifier,
            ) {
                ButtonContent(icon = icon, text = text)
            }
        }

        FocusGuardButtonVariant.Danger -> {
            OutlinedButton(
                onClick = onClick,
                shape = PillShape,
                enabled = enabled,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
                modifier = semanticsModifier,
            ) {
                ButtonContent(icon = icon, text = text)
            }
        }
    }
}

@Composable
private fun ButtonContent(icon: ImageVector?, text: String) {
    if (icon != null) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(6.dp))
    }
    Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
}
