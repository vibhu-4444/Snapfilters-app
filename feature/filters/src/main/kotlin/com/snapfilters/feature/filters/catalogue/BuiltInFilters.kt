package com.snapfilters.feature.filters.catalogue

import com.snapfilters.feature.filters.model.LutCube
import kotlin.math.pow

enum class FilterCategory(val displayName: String) {
    CINEMATIC("Cinematic"),
    FILM("Film"),
    NATURAL("Natural")
}

data class FilterDescriptor(
    val id: String,
    val name: String,
    val category: FilterCategory,
    val description: String
)

object BuiltInFilters {

    val ALL: List<FilterDescriptor> = listOf(
        // Cinematic
        FilterDescriptor("meridian", "Meridian", FilterCategory.CINEMATIC, "Teal shadows, warm highlights, controlled roll-off"),
        FilterDescriptor("halide", "Halide", FilterCategory.CINEMATIC, "High contrast, deep shadows, gold highlights"),
        FilterDescriptor("nocturne", "Nocturne", FilterCategory.CINEMATIC, "Midnight blues, cinematic desaturation"),
        FilterDescriptor("tungsten", "Tungsten", FilterCategory.CINEMATIC, "Warm indoor tungsten with blue-balanced shadows"),

        // Film
        FilterDescriptor("portra_400", "Portra 400", FilterCategory.FILM, "Soft pastel tones, gentle highlight roll-off"),
        FilterDescriptor("ektar", "Ektar", FilterCategory.FILM, "Vivid saturated landscape tones, crisp contrast"),
        FilterDescriptor("tri_x", "Tri-X", FilterCategory.FILM, "High contrast monochrome, rich silver gradation"),
        FilterDescriptor("cine_still", "Cine Still", FilterCategory.FILM, "Warm glow highlights, green-cyan shadows"),

        // Natural
        FilterDescriptor("skin_true", "Skin True", FilterCategory.NATURAL, "Minimal hue shift, protected natural skin tones"),
        FilterDescriptor("daylight", "Daylight", FilterCategory.NATURAL, "Clean 5600K daylight balance, neutral whites"),
        FilterDescriptor("overcast", "Overcast", FilterCategory.NATURAL, "Cool neutral diffused light, lifted midtones"),
        FilterDescriptor("portrait_soft", "Portrait Soft", FilterCategory.NATURAL, "Soft portrait contrast, smoothed transitions")
    )

    private val cache = mutableMapOf<String, LutCube>()

    fun getLut(filterId: String, size: Int = 33): LutCube {
        return cache.getOrPut(filterId) {
            generateBuiltInLut(filterId, size)
        }
    }

    private fun generateBuiltInLut(filterId: String, size: Int): LutCube {
        val totalPoints = size * size * size
        val data = FloatArray(totalPoints * 3)
        var idx = 0

        val step = 1.0f / (size - 1)

        for (bIdx in 0 until size) {
            val b = bIdx * step
            for (gIdx in 0 until size) {
                val g = gIdx * step
                for (rIdx in 0 until size) {
                    val r = rIdx * step

                    val (outR, outG, outB) = transformColor(filterId, r, g, b)
                    data[idx++] = outR.coerceIn(0f, 1f)
                    data[idx++] = outG.coerceIn(0f, 1f)
                    data[idx++] = outB.coerceIn(0f, 1f)
                }
            }
        }

        val name = ALL.find { it.id == filterId }?.name ?: filterId
        return LutCube(
            title = name,
            size = size,
            data = data
        )
    }

    private fun transformColor(filterId: String, r: Float, g: Float, b: Float): Triple<Float, Float, Float> {
        val luma = 0.2126f * r + 0.7152f * g + 0.0722f * b

        return when (filterId) {
            "meridian" -> {
                // Teal shadows, warm highlights
                val shadowFactor = (1f - luma).pow(1.5f)
                val highlightFactor = luma.pow(1.5f)
                val outR = r * 0.95f + highlightFactor * 0.12f - shadowFactor * 0.05f
                val outG = g * 0.98f + highlightFactor * 0.05f + shadowFactor * 0.04f
                val outB = b * 0.92f - highlightFactor * 0.08f + shadowFactor * 0.14f
                Triple(outR, outG, outB)
            }
            "halide" -> {
                // S-curve contrast, gold highlights
                val sCurve = { v: Float -> (v.pow(1.2f) * 1.15f - 0.05f).coerceIn(0f, 1f) }
                val outR = sCurve(r) * 1.05f
                val outG = sCurve(g) * 0.98f
                val outB = sCurve(b) * 0.90f
                Triple(outR, outG, outB)
            }
            "nocturne" -> {
                // Midnight blues, desaturated reds
                val outR = (r * 0.70f + luma * 0.20f)
                val outG = (g * 0.85f + luma * 0.10f)
                val outB = (b * 1.15f).coerceAtMost(1f)
                Triple(outR, outG, outB)
            }
            "tungsten" -> {
                // Warm incandescent 3200K
                val outR = (r * 1.12f).coerceAtMost(1f)
                val outG = g * 1.02f
                val outB = b * 0.82f
                Triple(outR, outG, outB)
            }
            "portra_400" -> {
                // Soft highlights, warm skin tones
                val outR = (r.pow(0.95f) * 1.04f).coerceAtMost(1f)
                val outG = g.pow(0.98f) * 1.01f
                val outB = b.pow(1.04f) * 0.94f
                Triple(outR, outG, outB)
            }
            "ektar" -> {
                // High saturation, vibrant primaries
                val outR = (luma + (r - luma) * 1.35f).coerceIn(0f, 1f)
                val outG = (luma + (g - luma) * 1.25f).coerceIn(0f, 1f)
                val outB = (luma + (b - luma) * 1.30f).coerceIn(0f, 1f)
                Triple(outR, outG, outB)
            }
            "tri_x" -> {
                // High contrast black and white
                val contrastLuma = ((luma - 0.5f) * 1.35f + 0.5f).coerceIn(0f, 1f)
                Triple(contrastLuma, contrastLuma, contrastLuma)
            }
            "cine_still" -> {
                // Cyan shadows, warm halation
                val shadowFactor = (1f - luma).pow(1.8f)
                val highlightFactor = luma.pow(2.0f)
                val outR = r + highlightFactor * 0.15f
                val outG = g + shadowFactor * 0.06f
                val outB = b + shadowFactor * 0.08f - highlightFactor * 0.05f
                Triple(outR, outG, outB)
            }
            "skin_true" -> {
                // Minimal hue shift, faithfully protected skin
                val outR = r * 1.01f
                val outG = g * 1.00f
                val outB = b * 0.99f
                Triple(outR, outG, outB)
            }
            "daylight" -> {
                // 5600K neutral daylight
                Triple(r, g, b)
            }
            "overcast" -> {
                // Cool diffused light, lifted shadows
                val outR = r * 0.96f + 0.04f
                val outG = g * 0.98f + 0.04f
                val outB = b * 1.04f + 0.05f
                Triple(outR, outG, outB)
            }
            "portrait_soft" -> {
                // Soft roll-off
                val outR = (r.pow(0.96f) * 1.02f).coerceAtMost(1f)
                val outG = (g.pow(0.97f) * 1.01f).coerceAtMost(1f)
                val outB = (b.pow(0.98f)).coerceAtMost(1f)
                Triple(outR, outG, outB)
            }
            else -> Triple(r, g, b)
        }
    }
}
