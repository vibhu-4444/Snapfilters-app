package com.snapfilters.feature.camera.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.camera.view.PreviewView
import com.snapfilters.core.designsystem.component.ChipState
import com.snapfilters.core.designsystem.component.ModeChip
import com.snapfilters.core.designsystem.component.ShutterButton
import com.snapfilters.core.designsystem.component.ShutterState
import com.snapfilters.core.designsystem.theme.SnapTheme
import com.snapfilters.feature.camera.model.CameraAspectRatio
import com.snapfilters.feature.camera.model.FlashMode

/**
 * CameraScreen provides the primary camera interface per PRD v3.0 Chapter 6, 7 & 13.
 * Supports:
 * - FR-CAM-01: Front / Rear lens switch
 * - FR-CAM-02: Sensor resolution capture
 * - FR-CAM-03: Flash mode toggle (hidden when sensor has no flash unit)
 * - FR-CAM-04: Digital zoom clamping
 * - FR-CAM-05: Manual EV compensation slider
 * - FR-CAM-06: Tap to focus and focus lock
 * - FR-CAM-07: Rule-of-thirds grid
 * - FR-CAM-08: 3s/10s timer with cancellation on back press
 * - FR-CAM-09: Aspect ratio switcher (4:3, 16:9, 1:1)
 */
@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onPreviewViewReady: (PreviewView) -> Unit,
    onNavigateToLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // FR-CAM-08: Countdown cancels on back press
    BackHandler(enabled = uiState.isCountingDown) {
        viewModel.cancelCountdown()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SnapTheme.colors.surfaceSunken)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Controls Bar
            CameraTopBar(
                uiState = uiState,
                onFlashToggle = viewModel::cycleFlashMode,
                onAspectRatioToggle = viewModel::cycleAspectRatio,
                onTimerToggle = viewModel::cycleTimerMode,
                onGridToggle = viewModel::toggleGrid,
                onToggleExposure = viewModel::toggleExposureSlider,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = SnapTheme.spacing.md, vertical = SnapTheme.spacing.sm)
            )

            // Viewfinder Center Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CameraViewfinder(
                    aspectRatio = uiState.settings.aspectRatio,
                    isGridVisible = uiState.settings.isGridVisible,
                    onPreviewViewReady = onPreviewViewReady,
                    onTapToFocus = viewModel::onFocusTapped,
                    onPinchZoom = viewModel::setZoomRatio,
                    modifier = Modifier.fillMaxWidth()
                )

                // Exposure Compensation Slider (FR-CAM-05)
                if (uiState.showExposureSlider && uiState.capabilities.supportsExposureCompensation) {
                    ExposureSlider(
                        uiState = uiState,
                        onExposureChange = viewModel::setExposureCompensation,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = SnapTheme.spacing.md)
                    )
                }
            }

            // Bottom Controls Area
            CameraBottomBar(
                uiState = uiState,
                onShutterClick = viewModel::onShutterPressed,
                onLensSwitch = viewModel::switchLens,
                onFocusLockToggle = viewModel::toggleFocusLock,
                onNavigateToLibrary = onNavigateToLibrary,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SnapTheme.colors.surfaceBase)
                    .navigationBarsPadding()
                    .padding(vertical = SnapTheme.spacing.lg, horizontal = SnapTheme.spacing.xl)
            )
        }
    }
}

@Composable
private fun CameraTopBar(
    uiState: CameraUiState,
    onFlashToggle: () -> Unit,
    onAspectRatioToggle: () -> Unit,
    onTimerToggle: () -> Unit,
    onGridToggle: () -> Unit,
    onToggleExposure: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flash control (FR-CAM-03): Completely hidden if flash unit is absent
        if (uiState.capabilities.hasFlashUnit) {
            IconButton(onClick = onFlashToggle) {
                val icon = when (uiState.settings.flashMode) {
                    FlashMode.Off -> Icons.Default.FlashOff
                    FlashMode.Auto -> Icons.Default.FlashAuto
                    FlashMode.On, FlashMode.Torch -> Icons.Default.FlashOn
                }
                val tint = if (uiState.settings.flashMode != FlashMode.Off) SnapTheme.colors.accent else SnapTheme.colors.textPrimary
                Icon(imageVector = icon, contentDescription = "Toggle Flash", tint = tint)
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }

        // Aspect ratio (FR-CAM-09)
        ModeChip(
            text = uiState.settings.aspectRatio.displayName,
            state = ChipState.Selected,
            onClick = onAspectRatioToggle
        )

        // Timer (FR-CAM-08)
        ModeChip(
            text = uiState.settings.timerMode.displayName,
            state = if (uiState.settings.timerMode.seconds > 0) ChipState.Selected else ChipState.Idle,
            onClick = onTimerToggle
        )

        // Grid (FR-CAM-07)
        IconButton(onClick = onGridToggle) {
            val tint = if (uiState.settings.isGridVisible) SnapTheme.colors.accent else SnapTheme.colors.textSecondary
            Icon(imageVector = Icons.Default.GridOn, contentDescription = "Toggle Grid", tint = tint)
        }

        // EV slider toggle (FR-CAM-05)
        if (uiState.capabilities.supportsExposureCompensation) {
            IconButton(onClick = onToggleExposure) {
                val tint = if (uiState.showExposureSlider) SnapTheme.colors.accent else SnapTheme.colors.textSecondary
                Icon(imageVector = Icons.Default.Tune, contentDescription = "Exposure Compensation", tint = tint)
            }
        }
    }
}

@Composable
private fun ExposureSlider(
    uiState: CameraUiState,
    onExposureChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val caps = uiState.capabilities
    val current = uiState.settings.exposureCompensationIndex.toFloat()

    Column(
        modifier = modifier
            .clip(SnapTheme.shapes.control)
            .background(SnapTheme.colors.surfaceRaised.copy(alpha = 0.85f))
            .padding(SnapTheme.spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val evValue = current * caps.exposureCompensationStep
        Text(
            text = String.format("%+.1f EV", evValue),
            style = SnapTheme.typography.caption,
            color = SnapTheme.colors.textPrimary
        )

        Slider(
            value = current,
            onValueChange = { onExposureChange(it.toInt()) },
            valueRange = caps.minExposureIndex.toFloat()..caps.maxExposureIndex.toFloat(),
            steps = (caps.maxExposureIndex - caps.minExposureIndex - 1).coerceAtLeast(0),
            colors = SliderDefaults.colors(
                thumbColor = SnapTheme.colors.accent,
                activeTrackColor = SnapTheme.colors.accent,
                inactiveTrackColor = SnapTheme.colors.surfaceSunken
            ),
            modifier = Modifier.width(120.dp)
        )
    }
}

@Composable
private fun CameraBottomBar(
    uiState: CameraUiState,
    onShutterClick: () -> Unit,
    onLensSwitch: () -> Unit,
    onFocusLockToggle: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Library pill button
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SnapTheme.colors.surfaceRaised)
                .clickable(onClick = onNavigateToLibrary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = "Photo Library",
                tint = SnapTheme.colors.textPrimary
            )
        }

        // Shutter Button (72 dp ring, 60 dp inner disc)
        val shutterState = when {
            uiState.isCountingDown -> ShutterState.TimerCounting
            uiState.isTakingPicture -> ShutterState.Pressed
            else -> ShutterState.Idle
        }

        ShutterButton(
            state = shutterState,
            timerSecondsRemaining = uiState.countdownSecondsRemaining,
            onClick = onShutterClick
        )

        // Controls container: Lens Switch (FR-CAM-01) & Focus Lock (FR-CAM-06)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (uiState.capabilities.supportsFocusLock) {
                IconButton(onClick = onFocusLockToggle) {
                    val tint = if (uiState.settings.isFocusLocked) SnapTheme.colors.accent else SnapTheme.colors.textSecondary
                    val icon = if (uiState.settings.isFocusLocked) Icons.Default.Lock else Icons.Default.LockOpen
                    Icon(imageVector = icon, contentDescription = "Focus Lock", tint = tint)
                }
            }

            IconButton(onClick = onLensSwitch) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Switch Camera",
                    tint = SnapTheme.colors.textPrimary
                )
            }
        }
    }
}

