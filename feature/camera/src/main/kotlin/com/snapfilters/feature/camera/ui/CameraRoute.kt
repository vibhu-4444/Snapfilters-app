package com.snapfilters.feature.camera.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snapfilters.feature.camera.model.CaptureResult

/**
 * CameraRoute published by feature-camera per PRD v3.0 Chapter 5 & 10.
 * Entry point consumed by the App NavHost.
 */
@Composable
fun CameraRoute(
    onNavigateToEditor: (CaptureResult) -> Unit,
    onNavigateToLibrary: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = viewModel()
) {
    CameraScreen(
        viewModel = viewModel,
        onPreviewViewReady = { /* Bound to CameraController in host activity */ },
        onNavigateToLibrary = onNavigateToLibrary,
        modifier = modifier
    )
}

