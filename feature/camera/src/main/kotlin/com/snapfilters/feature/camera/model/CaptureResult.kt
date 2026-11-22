package com.snapfilters.feature.camera.model

import android.net.Uri

/**
 * CaptureResult published by feature-camera per PRD v3.0 Chapter 5 & 13.
 * Represents the captured image file and its metadata.
 */
data class CaptureResult(
    val uri: Uri,
    val width: Int,
    val height: Int,
    val orientation: Int,
    val filterId: String? = null,
    val intensity: Float = 1.0f
)

