package com.snapfilters.feature.camera

import com.snapfilters.core.common.dispatchers.CoroutineDispatchers
import com.snapfilters.feature.camera.core.CameraCapabilityProber
import com.snapfilters.feature.camera.model.CameraAspectRatio
import com.snapfilters.feature.camera.model.CameraCapabilities
import com.snapfilters.feature.camera.model.FlashMode
import com.snapfilters.feature.camera.model.LensFacing
import com.snapfilters.feature.camera.model.TimerMode
import com.snapfilters.feature.camera.ui.CameraUiEvent
import com.snapfilters.feature.camera.ui.CameraViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CameraViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val mockDispatchers = object : CoroutineDispatchers {
        override val main: CoroutineDispatcher get() = testDispatcher
        override val io: CoroutineDispatcher get() = testDispatcher
        override val default: CoroutineDispatcher get() = testDispatcher
        override val unconfined: CoroutineDispatcher get() = testDispatcher
    }

    private lateinit var mockCapabilityProber: CameraCapabilityProber

    private val defaultCapabilities = CameraCapabilities(
        hasFlashUnit = true,
        supportsAutoFocus = true,
        supportsFocusLock = true,
        supportsExposureCompensation = true,
        minExposureIndex = -4,
        maxExposureIndex = 4,
        exposureCompensationStep = 0.5f,
        minZoomRatio = 1.0f,
        maxZoomRatio = 5.0f
    )

    @Before
    fun setUp() {
        mockCapabilityProber = mockk(relaxed = true)
        every { mockCapabilityProber.probeCapabilities(any()) } returns defaultCapabilities
    }

    // FR-CAM-01: Front / Rear lens switch
    @Test
    fun fr_cam_01_lens_switch_toggles_between_back_and_front() {
        val viewModel = CameraViewModel(mockCapabilityProber, mockDispatchers)
        assertEquals(LensFacing.Back, viewModel.uiState.value.settings.lensFacing)

        viewModel.switchLens()
        assertEquals(LensFacing.Front, viewModel.uiState.value.settings.lensFacing)

        viewModel.switchLens()
        assertEquals(LensFacing.Back, viewModel.uiState.value.settings.lensFacing)
    }

    // FR-CAM-03: Flash mode cycle and hardware absence handling
    @Test
    fun fr_cam_03_flash_mode_cycles_when_hardware_supports() {
        val viewModel = CameraViewModel(mockCapabilityProber, mockDispatchers)
        assertEquals(FlashMode.Auto, viewModel.uiState.value.settings.flashMode)

        viewModel.cycleFlashMode()
        assertEquals(FlashMode.On, viewModel.uiState.value.settings.flashMode)

        viewModel.cycleFlashMode()
        assertEquals(FlashMode.Torch, viewModel.uiState.value.settings.flashMode)

        viewModel.cycleFlashMode()
        assertEquals(FlashMode.Off, viewModel.uiState.value.settings.flashMode)

        viewModel.cycleFlashMode()
        assertEquals(FlashMode.Auto, viewModel.uiState.value.settings.flashMode)
    }

    @Test
    fun fr_cam_03_flash_mode_cannot_be_cycled_when_hardware_lacks_flash() {
        val noFlashCapabilities = defaultCapabilities.copy(hasFlashUnit = false)
        every { mockCapabilityProber.probeCapabilities(any()) } returns noFlashCapabilities

        val viewModel = CameraViewModel(mockCapabilityProber, mockDispatchers)
        val initialFlash = viewModel.uiState.value.settings.flashMode

        viewModel.cycleFlashMode()
        assertEquals(initialFlash, viewModel.uiState.value.settings.flashMode)
    }

    // FR-CAM-04: Digital zoom clamped to max
    @Test
    fun fr_cam_04_zoom_ratio_clamped_to_hardware_max_zoom() {
        val viewModel = CameraViewModel(mockCapabilityProber, mockDispatchers)

        viewModel.setZoomRatio(10.0f) // Requested 10x, but max is 5.0f
        assertEquals(5.0f, viewModel.uiState.value.settings.zoomRatio, 0.001f)

        viewModel.setZoomRatio(0.5f) // Requested 0.5x, but min is 1.0f
        assertEquals(1.0f, viewModel.uiState.value.settings.zoomRatio, 0.001f)
    }

    // FR-CAM-05: Exposure compensation within bounds
    @Test
    fun fr_cam_05_exposure_compensation_clamped_to_ev_range() {
        val viewModel = CameraViewModel(mockCapabilityProber, mockDispatchers)

        viewModel.setExposureCompensation(8) // max is 4
        assertEquals(4, viewModel.uiState.value.settings.exposureCompensationIndex)

        viewModel.setExposureCompensation(-10) // min is -4
        assertEquals(-4, viewModel.uiState.value.settings.exposureCompensationIndex)
    }

    // FR-CAM-06: Tap to focus and focus lock
    @Test
    fun fr_cam_06_tap_to_focus_and_focus_lock() {
        val viewModel = CameraViewModel(mockCapabilityProber, mockDispatchers)
        assertNull(viewModel.uiState.value.focusPoint)
        assertFalse(viewModel.uiState.value.settings.isFocusLocked)

        viewModel.onFocusTapped(0.5f, 0.5f)
        assertEquals(Pair(0.5f, 0.5f), viewModel.uiState.value.focusPoint)

        viewModel.toggleFocusLock()
        assertTrue(viewModel.uiState.value.settings.isFocusLocked)
    }

    // FR-CAM-07: Rule of thirds grid off by default
    @Test
    fun fr_cam_07_grid_off_by_default_and_toggles() {
        val viewModel = CameraViewModel(mockCapabilityProber, mockDispatchers)
        assertFalse("Grid must be off by default per FR-CAM-07", viewModel.uiState.value.settings.isGridVisible)

        viewModel.toggleGrid()
        assertTrue(viewModel.uiState.value.settings.isGridVisible)

        viewModel.toggleGrid()
        assertFalse(viewModel.uiState.value.settings.isGridVisible)
    }

    // FR-CAM-08: Timer countdown cancels on cancel request (Back press)
    @Test
    fun fr_cam_08_timer_cancellable_on_back_press() = testScope.runTest {
        val viewModel = CameraViewModel(mockCapabilityProber, mockDispatchers)
        viewModel.cycleTimerMode() // Sec3
        assertEquals(TimerMode.Sec3, viewModel.uiState.value.settings.timerMode)

        viewModel.onShutterPressed()
        testScheduler.advanceTimeBy(100)
        assertTrue(viewModel.uiState.value.isCountingDown)
        assertEquals(3, viewModel.uiState.value.countdownSecondsRemaining)

        // Simulate back press cancelling countdown
        val cancelled = viewModel.cancelCountdown()
        assertTrue(cancelled)
        assertFalse(viewModel.uiState.value.isCountingDown)
        assertNull(viewModel.uiState.value.countdownSecondsRemaining)
    }

    // FR-CAM-09: Aspect ratio cycling (4:3, 16:9, 1:1)
    @Test
    fun fr_cam_09_aspect_ratio_cycles_correctly() {
        val viewModel = CameraViewModel(mockCapabilityProber, mockDispatchers)
        assertEquals(CameraAspectRatio.Ratio4_3, viewModel.uiState.value.settings.aspectRatio)

        viewModel.cycleAspectRatio()
        assertEquals(CameraAspectRatio.Ratio16_9, viewModel.uiState.value.settings.aspectRatio)

        viewModel.cycleAspectRatio()
        assertEquals(CameraAspectRatio.Ratio1_1, viewModel.uiState.value.settings.aspectRatio)

        viewModel.cycleAspectRatio()
        assertEquals(CameraAspectRatio.Ratio4_3, viewModel.uiState.value.settings.aspectRatio)
    }
}
