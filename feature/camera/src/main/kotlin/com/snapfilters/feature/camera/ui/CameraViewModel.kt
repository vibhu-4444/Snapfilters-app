package com.snapfilters.feature.camera.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapfilters.core.common.dispatchers.CoroutineDispatchers
import com.snapfilters.core.common.dispatchers.DefaultCoroutineDispatchers
import com.snapfilters.feature.camera.core.CameraCapabilityProber
import com.snapfilters.feature.camera.model.CameraAspectRatio
import com.snapfilters.feature.camera.model.CameraCapabilities
import com.snapfilters.feature.camera.model.CameraSettings
import com.snapfilters.feature.camera.model.FlashMode
import com.snapfilters.feature.camera.model.LensFacing
import com.snapfilters.feature.camera.model.TimerMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CameraUiEvent {
    data object TriggerCapture : CameraUiEvent()
    data class ShowToast(val message: String) : CameraUiEvent()
    data object CancelCountdown : CameraUiEvent()
}

data class CameraUiState(
    val settings: CameraSettings = CameraSettings(),
    val capabilities: CameraCapabilities = CameraCapabilities(),
    val isTakingPicture: Boolean = false,
    val isCountingDown: Boolean = false,
    val countdownSecondsRemaining: Int? = null,
    val showExposureSlider: Boolean = false,
    val focusPoint: Pair<Float, Float>? = null // (x, y) in 0f..1f coords
)

class CameraViewModel(
    private val capabilityProber: CameraCapabilityProber? = null,
    private val dispatchers: CoroutineDispatchers = DefaultCoroutineDispatchers()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CameraUiEvent>()
    val events: SharedFlow<CameraUiEvent> = _events.asSharedFlow()

    private var countdownJob: Job? = null

    init {
        refreshCapabilities()
    }

    fun refreshCapabilities() {
        val currentFacing = _uiState.value.settings.lensFacing
        val capabilities = capabilityProber?.probeCapabilities(currentFacing) ?: CameraCapabilities(
            hasFlashUnit = true,
            supportsAutoFocus = true,
            supportsFocusLock = true,
            supportsExposureCompensation = true,
            minExposureIndex = -4,
            maxExposureIndex = 4,
            exposureCompensationStep = 0.5f,
            minZoomRatio = 1.0f,
            maxZoomRatio = 8.0f
        )
        _uiState.update { it.copy(capabilities = capabilities) }
    }

    // FR-CAM-01: Front / Rear switch
    fun switchLens() {
        val newFacing = if (_uiState.value.settings.lensFacing == LensFacing.Back) {
            LensFacing.Front
        } else {
            LensFacing.Back
        }
        val capabilities = capabilityProber?.probeCapabilities(newFacing) ?: _uiState.value.capabilities
        _uiState.update { state ->
            state.copy(
                settings = state.settings.copy(
                    lensFacing = newFacing,
                    zoomRatio = 1.0f,
                    exposureCompensationIndex = 0,
                    isFocusLocked = false
                ),
                capabilities = capabilities,
                focusPoint = null
            )
        }
    }

    // FR-CAM-03: Flash mode toggle (Persisted across sessions)
    fun cycleFlashMode() {
        if (!_uiState.value.capabilities.hasFlashUnit) return
        val current = _uiState.value.settings.flashMode
        val next = when (current) {
            FlashMode.Off -> FlashMode.Auto
            FlashMode.Auto -> FlashMode.On
            FlashMode.On -> FlashMode.Torch
            FlashMode.Torch -> FlashMode.Off
        }
        _uiState.update { it.copy(settings = it.settings.copy(flashMode = next)) }
    }

    fun setFlashMode(flashMode: FlashMode) {
        if (!_uiState.value.capabilities.hasFlashUnit && flashMode != FlashMode.Off) return
        _uiState.update { it.copy(settings = it.settings.copy(flashMode = flashMode)) }
    }

    // FR-CAM-04: Pinch zoom clamped to device maximum
    fun setZoomRatio(ratio: Float) {
        val caps = _uiState.value.capabilities
        val clampedRatio = ratio.coerceIn(caps.minZoomRatio, caps.maxZoomRatio)
        _uiState.update { it.copy(settings = it.settings.copy(zoomRatio = clampedRatio)) }
    }

    // FR-CAM-05: Exposure compensation in EV steps
    fun setExposureCompensation(index: Int) {
        val caps = _uiState.value.capabilities
        if (!caps.supportsExposureCompensation) return
        val clamped = index.coerceIn(caps.minExposureIndex, caps.maxExposureIndex)
        _uiState.update { it.copy(settings = it.settings.copy(exposureCompensationIndex = clamped)) }
    }

    fun toggleExposureSlider() {
        if (!_uiState.value.capabilities.supportsExposureCompensation) return
        _uiState.update { it.copy(showExposureSlider = !it.showExposureSlider) }
    }

    // FR-CAM-06: Tap to focus and focus lock
    fun onFocusTapped(x: Float, y: Float) {
        if (!_uiState.value.capabilities.supportsAutoFocus) return
        _uiState.update { it.copy(focusPoint = Pair(x, y), settings = it.settings.copy(isFocusLocked = false)) }
    }

    fun toggleFocusLock() {
        val caps = _uiState.value.capabilities
        if (!caps.supportsFocusLock) return
        _uiState.update { it.copy(settings = it.settings.copy(isFocusLocked = !it.settings.isFocusLocked)) }
    }

    // FR-CAM-07: Rule of thirds grid toggle
    fun toggleGrid() {
        _uiState.update { it.copy(settings = it.settings.copy(isGridVisible = !it.settings.isGridVisible)) }
    }

    // FR-CAM-08: Timer mode cycle
    fun cycleTimerMode() {
        val current = _uiState.value.settings.timerMode
        val next = when (current) {
            TimerMode.Off -> TimerMode.Sec3
            TimerMode.Sec3 -> TimerMode.Sec10
            TimerMode.Sec10 -> TimerMode.Off
        }
        _uiState.update { it.copy(settings = it.settings.copy(timerMode = next)) }
    }

    // FR-CAM-09: Aspect ratio selection
    fun cycleAspectRatio() {
        val current = _uiState.value.settings.aspectRatio
        val next = when (current) {
            CameraAspectRatio.Ratio4_3 -> CameraAspectRatio.Ratio16_9
            CameraAspectRatio.Ratio16_9 -> CameraAspectRatio.Ratio1_1
            CameraAspectRatio.Ratio1_1 -> CameraAspectRatio.Ratio4_3
        }
        _uiState.update { it.copy(settings = it.settings.copy(aspectRatio = next)) }
    }

    fun setAspectRatio(ratio: CameraAspectRatio) {
        _uiState.update { it.copy(settings = it.settings.copy(aspectRatio = ratio)) }
    }

    // Shutter Pressed action
    fun onShutterPressed() {
        if (_uiState.value.isTakingPicture) return

        val timerMode = _uiState.value.settings.timerMode
        if (timerMode == TimerMode.Off) {
            triggerActualCapture()
        } else {
            startTimerCountdown(timerMode.seconds)
        }
    }

    private fun startTimerCountdown(seconds: Int) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch(dispatchers.main) {
            _uiState.update { it.copy(isCountingDown = true, countdownSecondsRemaining = seconds) }
            for (remaining in seconds downTo 1) {
                _uiState.update { it.copy(countdownSecondsRemaining = remaining) }
                delay(1000)
            }
            _uiState.update { it.copy(isCountingDown = false, countdownSecondsRemaining = null) }
            triggerActualCapture()
        }
    }

    fun cancelCountdown(): Boolean {
        if (_uiState.value.isCountingDown) {
            countdownJob?.cancel()
            countdownJob = null
            _uiState.update { it.copy(isCountingDown = false, countdownSecondsRemaining = null) }
            viewModelScope.launch {
                _events.emit(CameraUiEvent.CancelCountdown)
            }
            return true
        }
        return false
    }

    private fun triggerActualCapture() {
        _uiState.update { it.copy(isTakingPicture = true) }
        viewModelScope.launch {
            _events.emit(CameraUiEvent.TriggerCapture)
        }
    }

    fun onCaptureCompleted() {
        _uiState.update { it.copy(isTakingPicture = false) }
    }
}

