package com.snapfilters.feature.camera.model

data class CameraCapabilities(
    val hasFlashUnit: Boolean = false,
    val supportsAutoFocus: Boolean = false,
    val supportsFocusLock: Boolean = false,
    val supportsExposureCompensation: Boolean = false,
    val minExposureIndex: Int = 0,
    val maxExposureIndex: Int = 0,
    val exposureCompensationStep: Float = 0f,
    val minZoomRatio: Float = 1.0f,
    val maxZoomRatio: Float = 1.0f
) {
    val hasEvRange: Boolean
        get() = minExposureIndex != 0 || maxExposureIndex != 0
}

