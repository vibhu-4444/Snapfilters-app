package com.snapfilters.feature.camera.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import com.snapfilters.feature.camera.model.CameraAspectRatio

/**
 * CameraViewfinder renders the CameraX PreviewView and rule-of-thirds grid.
 * Grid lines are purely overlay UI and are never burned into the captured image (FR-CAM-07).
 */
@Composable
fun CameraViewfinder(
    aspectRatio: CameraAspectRatio,
    isGridVisible: Boolean,
    onPreviewViewReady: (PreviewView) -> Unit,
    onTapToFocus: (Float, Float) -> Unit,
    onPinchZoom: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val ratioFloat = when (aspectRatio) {
        CameraAspectRatio.Ratio4_3 -> 3f / 4f
        CameraAspectRatio.Ratio16_9 -> 9f / 16f
        CameraAspectRatio.Ratio1_1 -> 1f
    }

    var currentZoomScale by remember { mutableFloatStateOf(1.0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratioFloat),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        currentZoomScale *= zoom
                        onPinchZoom(currentZoomScale)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val normX = (offset.x / size.width).coerceIn(0f, 1f)
                        val normY = (offset.y / size.height).coerceIn(0f, 1f)
                        onTapToFocus(normX, normY)
                    }
                },
            factory = { context ->
                PreviewView(context).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                    onPreviewViewReady(this)
                }
            }
        )

        // FR-CAM-07: Rule-of-thirds grid overlay
        if (isGridVisible) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridColor = Color.White.copy(alpha = 0.35f)
                val strokeWidth = 1.dp.toPx()

                // 2 vertical lines
                val colStep = size.width / 3f
                drawLine(
                    color = gridColor,
                    start = Offset(colStep, 0f),
                    end = Offset(colStep, size.height),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = gridColor,
                    start = Offset(colStep * 2f, 0f),
                    end = Offset(colStep * 2f, size.height),
                    strokeWidth = strokeWidth
                )

                // 2 horizontal lines
                val rowStep = size.height / 3f
                drawLine(
                    color = gridColor,
                    start = Offset(0f, rowStep),
                    end = Offset(size.width, rowStep),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = gridColor,
                    start = Offset(0f, rowStep * 2f),
                    end = Offset(size.width, rowStep * 2f),
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}
