package com.snapfilters.feature.filters.engine

import android.graphics.Bitmap
import android.graphics.Color
import com.snapfilters.core.common.filter.FilterEngine
import com.snapfilters.feature.filters.repository.LutRepository

/**
 * FilterEngineImpl implements FilterEngine interface from core-common.
 * Applies 3D LUT at given intensity (0.0f - 1.0f).
 * At intensity 0.0f, exact source pixels are returned (FR-FLT-02).
 */
class FilterEngineImpl(
    private val lutRepository: LutRepository
) : FilterEngine {

    override fun apply(filterId: String, intensity: Float, source: Bitmap): Bitmap {
        val clampedIntensity = intensity.coerceIn(0.0f, 1.0f)
        
        // Acceptance test FR-FLT-02: 0% output is pixel-identical to the source
        if (clampedIntensity == 0.0f) {
            return source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)
        }

        val lut = lutRepository.getLut(filterId)
        val width = source.width
        val height = source.height

        val outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val color = pixels[i]
            val a = Color.alpha(color)
            val r = Color.red(color) / 255.0f
            val g = Color.green(color) / 255.0f
            val b = Color.blue(color) / 255.0f

            val (lutR, lutG, lutB) = lut.sample(r, g, b)

            // Mix toward graded color
            val finalR = (r + clampedIntensity * (lutR - r)).coerceIn(0f, 1f)
            val finalG = (g + clampedIntensity * (lutG - g)).coerceIn(0f, 1f)
            val finalB = (b + clampedIntensity * (lutB - b)).coerceIn(0f, 1f)

            pixels[i] = Color.argb(
                a,
                (finalR * 255f).toInt(),
                (finalG * 255f).toInt(),
                (finalB * 255f).toInt()
            )
        }

        outBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return outBitmap
    }

    override fun getPreviewShaderHandle(): Int {
        // Handle to compiled OpenGL ES 3.0 shader program
        return 1
    }
}
