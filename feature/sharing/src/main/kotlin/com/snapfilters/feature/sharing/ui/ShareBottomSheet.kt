package com.snapfilters.feature.sharing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.snapfilters.core.designsystem.theme.SnapTheme
import com.snapfilters.feature.sharing.model.ShareCopyStrings
import com.snapfilters.feature.sharing.model.ShareMode

/**
 * ShareBottomSheet per PRD v3.0 Chapter 6, 7 & 17:
 * - Distinguishes between Share Photo and Share Original File
 * - Transparent UI copy explaining receiver recompression limits (FR-SHR-03)
 */
@Composable
fun ShareBottomSheet(
    onSelectShareMode: (ShareMode) -> Unit,
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
            text = "Share Photograph",
            style = SnapTheme.typography.title,
            color = SnapTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(SnapTheme.spacing.md))

        // Option 1: Share Photo (Compressed/optimized for messaging)
        ShareOptionRow(
            icon = Icons.Default.Image,
            title = "Share Photo",
            description = ShareCopyStrings.PHOTO_EXPLANATION,
            disclaimer = ShareCopyStrings.RECEIVER_DISCLAIMER,
            onClick = { onSelectShareMode(ShareMode.PHOTO) }
        )

        Spacer(modifier = Modifier.height(SnapTheme.spacing.sm))

        // Option 2: Share Original File (Untouched bytes)
        ShareOptionRow(
            icon = Icons.Default.InsertDriveFile,
            title = "Share Original File",
            description = ShareCopyStrings.ORIGINAL_EXPLANATION,
            disclaimer = ShareCopyStrings.RECEIVER_DISCLAIMER,
            onClick = { onSelectShareMode(ShareMode.ORIGINAL_FILE) }
        )
    }
}

@Composable
private fun ShareOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    disclaimer: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SnapTheme.shapes.control)
            .clickable(onClick = onClick)
            .padding(SnapTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SnapTheme.colors.accent,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(SnapTheme.spacing.md))

        Column {
            Text(
                text = title,
                style = SnapTheme.typography.body,
                color = SnapTheme.colors.textPrimary
            )
            Text(
                text = description,
                style = SnapTheme.typography.caption,
                color = SnapTheme.colors.textSecondary
            )
            Text(
                text = disclaimer,
                style = SnapTheme.typography.caption,
                color = SnapTheme.colors.textTertiary
            )
        }
    }
}
