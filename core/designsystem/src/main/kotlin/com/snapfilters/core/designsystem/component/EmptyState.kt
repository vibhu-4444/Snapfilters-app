package com.snapfilters.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.snapfilters.core.designsystem.theme.SnapTheme

/**
 * EmptyState per PRD v3.0 Chapter 7 & 12:
 * - Icon, one line of text, one button
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SnapTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SnapTheme.colors.textSecondary,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(SnapTheme.spacing.md))

        Text(
            text = message,
            style = SnapTheme.typography.body,
            color = SnapTheme.colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(SnapTheme.spacing.lg))

        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(
                containerColor = SnapTheme.colors.surfaceRaised,
                contentColor = SnapTheme.colors.accent
            ),
            shape = SnapTheme.shapes.control
        ) {
            Text(text = actionLabel, style = SnapTheme.typography.label)
        }
    }
}
