package com.snapfilters.feature.camera.model

enum class LensFacing {
    Back,
    Front
}

enum class FlashMode {
    Off,
    Auto,
    On,
    Torch
}

enum class CameraAspectRatio(val displayName: String, val ratioWidth: Int, val ratioHeight: Int) {
    Ratio4_3("4:3", 4, 3),
    Ratio16_9("16:9", 16, 9),
    Ratio1_1("1:1", 1, 1)
}

enum class TimerMode(val seconds: Int, val displayName: String) {
    Off(0, "OFF"),
    Sec3(3, "3s"),
    Sec10(10, "10s")
}

data class CameraSettings(
    val lensFacing: LensFacing = LensFacing.Back,
    val flashMode: FlashMode = FlashMode.Auto,
    val aspectRatio: CameraAspectRatio = CameraAspectRatio.Ratio4_3,
    val timerMode: TimerMode = TimerMode.Off,
    val isGridVisible: Boolean = false,
    val zoomRatio: Float = 1.0f,
    val exposureCompensationIndex: Int = 0,
    val isFocusLocked: Boolean = false
)
