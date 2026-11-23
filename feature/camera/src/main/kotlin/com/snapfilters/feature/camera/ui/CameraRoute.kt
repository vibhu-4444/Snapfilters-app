package com.snapfilters.feature.camera.ui

import android.net.Uri
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snapfilters.feature.camera.model.CaptureResult
import com.snapfilters.feature.camera.model.FlashMode
import com.snapfilters.feature.camera.model.LensFacing
import java.io.File

/**
 * CameraRoute published by feature-camera per PRD v3.0 Chapter 5 & 10.
 * Directly binds the hardware CameraX lifecycle, viewfinder surface, and shutter actions.
 */
@Composable
fun CameraRoute(
    onNavigateToEditor: (CaptureResult) -> Unit,
    onNavigateToLibrary: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    var previewViewInstance by remember { mutableStateOf<PreviewView?>(null) }
    var activeCamera by remember { mutableStateOf<Camera?>(null) }
    var imageCaptureUseCase by remember { mutableStateOf<ImageCapture?>(null) }

    // Bind and Re-bind CameraX when lens facing changes or preview view becomes ready
    LaunchedEffect(previewViewInstance, uiState.settings.lensFacing) {
        val pv = previewViewInstance ?: return@LaunchedEffect
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()

                val cameraSelector = if (uiState.settings.lensFacing == LensFacing.Front) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                val previewUseCase = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(pv.surfaceProvider)
                    }

                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setFlashMode(
                        when (uiState.settings.flashMode) {
                            FlashMode.On -> ImageCapture.FLASH_MODE_ON
                            FlashMode.Auto -> ImageCapture.FLASH_MODE_AUTO
                            FlashMode.Torch, FlashMode.Off -> ImageCapture.FLASH_MODE_OFF
                        }
                    )
                    .build()
                imageCaptureUseCase = imageCapture

                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    previewUseCase,
                    imageCapture
                )
                activeCamera = camera

                // Sync initial zoom ratio & exposure
                camera.cameraControl.setZoomRatio(uiState.settings.zoomRatio)
                if (uiState.capabilities.supportsExposureCompensation) {
                    camera.cameraControl.setExposureCompensationIndex(uiState.settings.exposureCompensationIndex)
                }

            } catch (e: Exception) {
                Log.e("SnapfiltersCamera", "Failed to bind CameraX use cases", e)
            }
        }, mainExecutor)
    }

    // Sync zoom adjustments in real-time
    LaunchedEffect(uiState.settings.zoomRatio) {
        try {
            activeCamera?.cameraControl?.setZoomRatio(uiState.settings.zoomRatio)
        } catch (e: Exception) {
            Log.e("SnapfiltersCamera", "Zoom adjustment failed", e)
        }
    }

    // Sync exposure adjustments in real-time
    LaunchedEffect(uiState.settings.exposureCompensationIndex) {
        try {
            if (uiState.capabilities.supportsExposureCompensation) {
                activeCamera?.cameraControl?.setExposureCompensationIndex(uiState.settings.exposureCompensationIndex)
            }
        } catch (e: Exception) {
            Log.e("SnapfiltersCamera", "Exposure adjustment failed", e)
        }
    }

    // Sync flash mode
    LaunchedEffect(uiState.settings.flashMode) {
        try {
            imageCaptureUseCase?.flashMode = when (uiState.settings.flashMode) {
                FlashMode.On -> ImageCapture.FLASH_MODE_ON
                FlashMode.Auto -> ImageCapture.FLASH_MODE_AUTO
                FlashMode.Torch -> {
                    activeCamera?.cameraControl?.enableTorch(true)
                    ImageCapture.FLASH_MODE_OFF
                }
                FlashMode.Off -> {
                    activeCamera?.cameraControl?.enableTorch(false)
                    ImageCapture.FLASH_MODE_OFF
                }
            }
        } catch (e: Exception) {
            Log.e("SnapfiltersCamera", "Flash mode adjustment failed", e)
        }
    }

    // Handle Tap-to-Focus
    LaunchedEffect(uiState.focusPoint) {
        val focus = uiState.focusPoint ?: return@LaunchedEffect
        val pv = previewViewInstance ?: return@LaunchedEffect
        try {
            val factory = pv.meteringPointFactory
            val point = factory.createPoint(focus.first * pv.width, focus.second * pv.height)
            val action = FocusMeteringAction.Builder(point).build()
            activeCamera?.cameraControl?.startFocusAndMetering(action)
        } catch (e: Exception) {
            Log.e("SnapfiltersCamera", "Focus metering failed", e)
        }
    }

    // Handle photo capture events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CameraUiEvent.TriggerCapture -> {
                    val captureUseCase = imageCaptureUseCase ?: return@collect
                    val photoFile = File(
                        context.cacheDir,
                        "SNAP_${System.currentTimeMillis()}.jpg"
                    )
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                    val mainExecutor = ContextCompat.getMainExecutor(context)

                    captureUseCase.takePicture(
                        outputOptions,
                        mainExecutor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                val uri = output.savedUri ?: Uri.fromFile(photoFile)
                                onNavigateToEditor(
                                    CaptureResult(
                                        uri = uri,
                                        width = 0,
                                        height = 0,
                                        orientation = 0
                                    )
                                )
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e("SnapfiltersCamera", "Photo capture failed", exception)
                            }
                        }
                    )
                }
                else -> {}
            }
        }
    }

    CameraScreen(
        viewModel = viewModel,
        onPreviewViewReady = { pv ->
            previewViewInstance = pv
        },
        onNavigateToLibrary = onNavigateToLibrary,
        modifier = modifier
    )
}
