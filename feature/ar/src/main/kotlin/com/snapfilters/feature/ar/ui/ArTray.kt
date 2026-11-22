package com.snapfilters.feature.ar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.snapfilters.core.designsystem.theme.SnapTheme
import com.snapfilters.feature.ar.engine.ArEffect

/**
 * ArTray per PRD v3.0 Chapter 6 & 7:
 * - Effect thumbnails with identical visual weight to filter tray
 * - Tap to apply
 * - Quiet hint when no faces are detected (never a modal)
 */
@Composable
fun ArTray(
    selectedEffect: ArEffect,
    hasDetectedFaces: Boolean,
    onSelectEffect: (ArEffect) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SnapTheme.spacing.sm)
    ) {
        // Quiet hint when AR effect selected but no faces detected (Chapter 6 State matrix)
        if (selectedEffect != ArEffect.NONE && !hasDetectedFaces) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = SnapTheme.spacing.xs),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Looking for faces...",
                    style = SnapTheme.typography.caption,
                    color = SnapTheme.colors.textSecondary
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SnapTheme.spacing.md),
            contentPadding = PaddingValues(horizontal = SnapTheme.spacing.lg)
        ) {
            items(ArEffect.values()) { effect ->
                val isSelected = selectedEffect == effect
                ArEffectThumbnail(
                    name = effect.displayName,
                    isSelected = isSelected,
                    onClick = { onSelectEffect(effect) }
                )
            }
        }
    }
}

@Composable
private fun ArEffectThumbnail(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val ringModifier = if (isSelected) {
        Modifier.border(2.dp, SnapTheme.colors.accent, CircleShape)
    } else {
        Modifier
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .then(ringModifier)
                .background(SnapTheme.colors.surfaceSunken),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = SnapTheme.colors.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = name,
            style = SnapTheme.typography.caption,
            color = if (isSelected) SnapTheme.colors.accent else SnapTheme.colors.textSecondary
        )
    }
}
