package com.snapfilters.core.designsystem.component

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.snapfilters.core.designsystem.theme.SnapTheme

enum class ChipState {
    Idle,
    Selected,
    Disabled
}

/**
 * ModeChip per PRD v3.0 Chapter 7 Component Inventory:
 * - States: Idle, Selected, Disabled
 * - Selected state is accent ring, not fill
 * - Haptics on filter/mode selection
 */
@Composable
fun ModeChip(
    text: String,
    state: ChipState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val view = LocalView.current
    val shape = SnapTheme.shapes.control

    val borderModifier = when (state) {
        ChipState.Selected -> Modifier.border(width = 1.5.dp, color = SnapTheme.colors.accent, shape = shape)
        ChipState.Idle -> Modifier.border(width = 1.dp, color = SnapTheme.colors.surfaceRaised, shape = shape)
        ChipState.Disabled -> Modifier.border(width = 1.dp, color = SnapTheme.colors.textTertiary.copy(alpha = 0.3f), shape = shape)
    }

    val textColor = when (state) {
        ChipState.Selected -> SnapTheme.colors.textPrimary
        ChipState.Idle -> SnapTheme.colors.textSecondary
        ChipState.Disabled -> SnapTheme.colors.textTertiary
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(borderModifier)
            .clickable(
                enabled = state != ChipState.Disabled,
                role = Role.Button,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    onClick()
                }
            )
            .padding(horizontal = SnapTheme.spacing.md, vertical = SnapTheme.spacing.sm),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = SnapTheme.typography.label,
            color = textColor
        )
    }
}

