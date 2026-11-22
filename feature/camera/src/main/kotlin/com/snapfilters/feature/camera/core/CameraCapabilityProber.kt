package com.snapfilters.feature.camera.core

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import com.snapfilters.feature.camera.model.CameraCapabilities
import com.snapfilters.feature.camera.model.LensFacing

/**
 * Probes hardware capabilities via Camera2 characteristics per PRD v3.0 Chapter 13.
 * Prevents assuming all devices are alike.
 */
class CameraCapabilityProber(private val context: Context) {

    fun probeCapabilities(lensFacing: LensFacing): CameraCapabilities {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            ?: return CameraCapabilities()

        val targetFacing = when (lensFacing) {
            LensFacing.Back -> CameraCharacteristics.LENS_FACING_BACK
            LensFacing.Front -> CameraCharacteristics.LENS_FACING_FRONT
        }

        try {
            for (cameraId in cameraManager.cameraIdList) {
                val chars = cameraManager.getCameraCharacteristics(cameraId)
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                if (facing == targetFacing) {
                    val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                    
                    val afModes = chars.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES) ?: intArrayOf()
                    val supportsAf = afModes.any { 
                        it == CameraCharacteristics.CONTROL_AF_MODE_AUTO || 
                        it == CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_PICTURE 
                    }
                    val supportsFocusLock = supportsAf

                    val aeRange = chars.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
                    val aeStepRational = chars.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)
                    val aeStep = aeStepRational?.toFloat() ?: 0f
                    val minEv = aeRange?.lower ?: 0
                    val maxEv = aeRange?.upper ?: 0
                    val supportsEv = minEv != 0 || maxEv != 0

                    var minZoom = 1.0f
                    var maxZoom = 1.0f
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val zoomRange = chars.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)
                        if (zoomRange != null) {
                            minZoom = zoomRange.lower
                            maxZoom = zoomRange.upper
                        }
                    } else {
                        val legacyMaxZoom = chars.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) ?: 1.0f
                        maxZoom = legacyMaxZoom.coerceAtLeast(1.0f)
                    }

                    return CameraCapabilities(
                        hasFlashUnit = hasFlash,
                        supportsAutoFocus = supportsAf,
                        supportsFocusLock = supportsFocusLock,
                        supportsExposureCompensation = supportsEv,
                        minExposureIndex = minEv,
                        maxExposureIndex = maxEv,
                        exposureCompensationStep = aeStep,
                        minZoomRatio = minZoom,
                        maxZoomRatio = maxZoom
                    )
                }
            }
        } catch (e: Exception) {
            // Fallback gracefully on sensor lookup errors
        }

        return CameraCapabilities()
    }
}
