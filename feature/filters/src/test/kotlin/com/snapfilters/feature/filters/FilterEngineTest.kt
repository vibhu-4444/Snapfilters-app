package com.snapfilters.feature.filters

import android.graphics.Bitmap
import com.snapfilters.feature.filters.catalogue.BuiltInFilters
import com.snapfilters.feature.filters.engine.FilterEngineImpl
import com.snapfilters.feature.filters.repository.LutRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class FilterEngineTest {

    private lateinit var mockRepository: LutRepository
    private lateinit var filterEngine: FilterEngineImpl

    @Before
    fun setUp() {
        mockRepository = mockk()
        filterEngine = FilterEngineImpl(mockRepository)
    }

    // FR-FLT-01: Exactly 12 built-in filters across 3 categories
    @Test
    fun fr_flt_01_all_twelve_builtin_filters_exist() {
        assertEquals("Must provide exactly 12 built-in filters per PRD v3.0 Chapter 14", 12, BuiltInFilters.ALL.size)

        val categories = BuiltInFilters.ALL.map { it.category }.distinct()
        assertEquals("Must span 3 categories: Cinematic, Film, Natural", 3, categories.size)

        val filterIds = BuiltInFilters.ALL.map { it.id }.toSet()
        val expectedIds = setOf(
            "meridian", "halide", "nocturne", "tungsten",
            "portra_400", "ektar", "tri_x", "cine_still",
            "skin_true", "daylight", "overcast", "portrait_soft"
        )
        assertEquals(expectedIds, filterIds)
    }

    // FR-FLT-02: 0% intensity output is pixel-identical to source
    @Test
    fun fr_flt_02_zero_intensity_is_pixel_identical_to_source() {
        val width = 10
        val height = 10
        val sourceBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        sourceBitmap.setPixel(0, 0, android.graphics.Color.RED)
        sourceBitmap.setPixel(5, 5, android.graphics.Color.GREEN)
        sourceBitmap.setPixel(9, 9, android.graphics.Color.BLUE)

        val resultBitmap = filterEngine.apply("meridian", 0.0f, sourceBitmap)

        assertEquals("Width must match", sourceBitmap.width, resultBitmap.width)
        assertEquals("Height must match", sourceBitmap.height, resultBitmap.height)

        for (x in 0 until width) {
            for (y in 0 until height) {
                assertEquals(
                    "Pixel ($x, $y) must be identical at 0% intensity",
                    sourceBitmap.getPixel(x, y),
                    resultBitmap.getPixel(x, y)
                )
            }
        }
    }
}
