package com.snapfilters.feature.editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.snapfilters.core.designsystem.theme.SnapTheme

val BUILT_IN_FILTER_NAMES = listOf(
    "meridian" to "Meridian",
    "halide" to "Halide",
    "nocturne" to "Nocturne",
    "tungsten" to "Tungsten",
    "portra_400" to "Portra 400",
    "ektar" to "Ektar",
    "tri_x" to "Tri-X",
    "cine_still" to "Cine Still",
    "skin_true" to "Skin True",
    "daylight" to "Daylight",
    "overcast" to "Overcast",
    "portrait_soft" to "Portrait Soft"
)

/**
 * EditorScreen per PRD v3.0 Chapter 6, 7 & 16:
 * - Full-bleed photo
 * - Horizontal filter tray
 * - Filter intensity slider (0-100%, numeric percent displayed on drag only)
 * - Save bottom sheet
 */
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SnapTheme.colors.surfaceBase)
    ) {
        // Full bleed photo area
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Full-bleed Photo Preview",
                style = SnapTheme.typography.body,
                color = SnapTheme.colors.textSecondary
            )
        }

        // Top bar: Back and Save
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = SnapTheme.spacing.md, vertical = SnapTheme.spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back to camera",
                    tint = SnapTheme.colors.textPrimary
                )
            }

            Box(
                modifier = Modifier
                    .clip(SnapTheme.shapes.control)
                    .background(SnapTheme.colors.accent)
                    .clickable { viewModel.openSaveSheet() }
                    .padding(horizontal = SnapTheme.spacing.lg, vertical = SnapTheme.spacing.sm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Save",
                    style = SnapTheme.typography.label,
                    color = SnapTheme.colors.surfaceBase
                )
            }
        }

        // Bottom Controls: Filter tray and Intensity slider
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(SnapTheme.colors.surfaceRaised)
                .navigationBarsPadding()
                .padding(vertical = SnapTheme.spacing.md)
        ) {
            // Intensity Slider (PRD Chapter 7: Shows numeric percent on drag only)
            if (uiState.selectedFilterId != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SnapTheme.spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.isDraggingSlider) {
                        Text(
                            text = "${(uiState.filterIntensity * 100).toInt()}%",
                            style = SnapTheme.typography.caption,
                            color = SnapTheme.colors.accent
                        )
                    }

                    Slider(
                        value = uiState.filterIntensity,
                        onValueChange = {
                            viewModel.setDraggingSlider(true)
                            viewModel.setFilterIntensity(it)
                        },
                        onValueChangeFinished = {
                            viewModel.setDraggingSlider(false)
                        },
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = SnapTheme.colors.accent,
                            activeTrackColor = SnapTheme.colors.accent,
                            inactiveTrackColor = SnapTheme.colors.surfaceSunken
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(SnapTheme.spacing.sm))

            // Filter Tray
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SnapTheme.spacing.md),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = SnapTheme.spacing.lg)
            ) {
                items(BUILT_IN_FILTER_NAMES) { (filterId, name) ->
                    val isSelected = uiState.selectedFilterId == filterId
                    FilterThumbnail(
                        name = name,
                        isSelected = isSelected,
                        onClick = { viewModel.selectFilter(filterId) }
                    )
                }
            }
        }

        // Save Bottom Sheet
        if (uiState.showSaveSheet) {
            SaveBottomSheet(
                selectedMode = uiState.saveMode,
                onSelectMode = viewModel::setSaveMode,
                onConfirmSave = viewModel::confirmSave,
                onDismiss = viewModel::dismissSaveSheet,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun FilterThumbnail(
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
