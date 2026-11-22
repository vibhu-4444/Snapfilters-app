package com.snapfilters.core.common.filter

import android.graphics.Bitmap

/**
 * FilterEngine interface published via core-common per PRD v3.0 Chapter 5 & 14.
 * Both feature-editor and feature-camera consume this interface without depending on feature-filters.
 */
interface FilterEngine {
    /**
     * Applies a 3D LUT filter at the given intensity (0.0f - 1.0f).
     * At intensity 0.0f, the output must be pixel-identical to the source (FR-FLT-02).
     */
    fun apply(filterId: String, intensity: Float, source: Bitmap): Bitmap

    /**
     * Returns the OpenGL ES 3.0 shader program handle for the live preview pass.
     */
    fun getPreviewShaderHandle(): Int
}
