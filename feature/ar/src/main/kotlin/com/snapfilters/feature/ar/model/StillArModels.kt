package com.snapfilters.feature.ar.model

import android.graphics.RectF

data class FaceCropTransform(
    val cropRect: RectF,
    val cropWidth: Float,
    val cropHeight: Float,
    val sourceWidth: Float,
    val sourceHeight: Float
) {
    /**
     * Maps normalized crop coordinates (0f..1f) back to absolute source image space (px).
     * Per ADR-005: Eliminates scaling inaccuracies by resolving landmarks at native crop scale.
     */
    fun mapCropToSource(cropNormX: Float, cropNormY: Float): Point2D {
        val absX = cropRect.left + (cropNormX * cropWidth)
        val absY = cropRect.top + (cropNormY * cropHeight)
        return Point2D(absX, absY)
    }
}

data class CompositedFaceAccessory(
    val faceIndex: Int,
    val mappedLandmarks: FaceLandmarks,
    val faceWidthPx: Float,
    val interOcularDistancePx: Float,
    val rollAngleDegrees: Float
)
