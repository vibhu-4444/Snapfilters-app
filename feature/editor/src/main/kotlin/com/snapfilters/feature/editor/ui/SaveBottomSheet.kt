package com.snapfilters.feature.editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.snapfilters.core.database.entity.SaveMode
import com.snapfilters.core.designsystem.theme.SnapTheme

/**
 * SaveBottomSheet per PRD v3.0 Chapter 6, 7 & 16:
 * - 3 save modes: Original only / Filtered only / Both
 * - 20 dp top corners radius
 * - Format notes
 */
@Composable
fun SaveBottomSheet(
    selectedMode: SaveMode,
    onSelectMode: (SaveMode) -> Unit,
    onConfirmSave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(SnapTheme.shapes.sheet)
            .background(SnapTheme.colors.surfaceRaised)
            .padding(SnapTheme.spacing.lg)
            .navigationBarsPadding()
    ) {
        Text(
            text = "Save Photograph",
            style = SnapTheme.typography.title,
            color = SnapTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(SnapTheme.spacing.md))

        SaveModeOption(
            title = "Filtered only",
            subtitle = "Rendered cinematic grade (JPEG 95% quality)",
            isSelected = selectedMode == SaveMode.FILTERED_ONLY,
            onClick = { onSelectMode(SaveMode.FILTERED_ONLY) }
        )

        SaveModeOption(
            title = "Both (Recommended)",
            subtitle = "Saves untouched sensor original and cinematic grade",
            isSelected = selectedMode == SaveMode.BOTH,
            onClick = { onSelectMode(SaveMode.BOTH) }
        )

        SaveModeOption(
            title = "Original only",
            subtitle = "Byte-for-byte untouched camera sensor capture",
            isSelected = selectedMode == SaveMode.ORIGINAL_ONLY,
            onClick = { onSelectMode(SaveMode.ORIGINAL_ONLY) }
        )

        Spacer(modifier = Modifier.height(SnapTheme.spacing.lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SnapTheme.spacing.md)
        ) {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SnapTheme.colors.surfaceBase,
                    contentColor = SnapTheme.colors.textSecondary
                ),
                shape = SnapTheme.shapes.control,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Cancel", style = SnapTheme.typography.label)
            }

            Button(
                onClick = onConfirmSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SnapTheme.colors.accent,
                    contentColor = SnapTheme.colors.surfaceBase
                ),
                shape = SnapTheme.shapes.control,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Save", style = SnapTheme.typography.label)
            }
        }
    }
}

@Composable
private fun SaveModeOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = SnapTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = SnapTheme.colors.accent,
                unselectedColor = SnapTheme.colors.textTertiary
            )
        )

        Spacer(modifier = Modifier.width(SnapTheme.spacing.sm))

        Column {
            Text(
                text = title,
                style = SnapTheme.typography.body,
                color = SnapTheme.colors.textPrimary
            )
            Text(
                text = subtitle,
                style = SnapTheme.typography.caption,
                color = SnapTheme.colors.textSecondary
            )
        }
    }
}
