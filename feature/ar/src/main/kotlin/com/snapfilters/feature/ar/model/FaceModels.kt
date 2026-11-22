package com.snapfilters.feature.ar.model

data class Point2D(val x: Float, val y: Float)

data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = (right - left).coerceAtLeast(0f)
    val height: Float get() = (bottom - top).coerceAtLeast(0f)
    val area: Float get() = width * height
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f

    fun calculateIoU(other: BoundingBox): Float {
        val interLeft = maxOf(left, other.left)
        val interTop = maxOf(top, other.top)
        val interRight = minOf(right, other.right)
        val interBottom = minOf(bottom, other.bottom)

        val interWidth = (interRight - interLeft).coerceAtLeast(0f)
        val interHeight = (interBottom - interTop).coerceAtLeast(0f)
        val interArea = interWidth * interHeight

        val unionArea = area + other.area - interArea
        return if (unionArea > 0f) interArea / unionArea else 0f
    }
}

data class FaceLandmarks(
    val leftEye: Point2D,
    val rightEye: Point2D,
    val noseBridge: Point2D,
    val leftTemple: Point2D,
    val rightTemple: Point2D,
    val centroid: Point2D
) {
    val interOcularDistance: Float
        get() = kotlin.math.hypot(rightEye.x - leftEye.x, rightEye.y - leftEye.y)

    val rollAngleDegrees: Float
        get() = Math.toDegrees(
            kotlin.math.atan2(
                (rightEye.y - leftEye.y).toDouble(),
                (rightEye.x - leftEye.x).toDouble()
            )
        ).toFloat()
}

data class DetectedFace(
    val boundingBox: BoundingBox,
    val landmarks: FaceLandmarks
)

enum class DeviceArTier(val maxLiveFaces: Int, val analysisWidth: Int, val analysisHeight: Int) {
    TIER_A(4, 640, 480), // Flagship (8 GB+ RAM)
    TIER_B(3, 480, 360), // Mid-range (6 GB RAM)
    TIER_C(2, 360, 270)  // Entry-level (<= 4 GB RAM)
}
