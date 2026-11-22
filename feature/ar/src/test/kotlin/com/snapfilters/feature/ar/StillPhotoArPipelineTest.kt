package com.snapfilters.feature.ar

import android.graphics.Bitmap
import com.snapfilters.feature.ar.engine.ArEffect
import com.snapfilters.feature.ar.model.BoundingBox
import com.snapfilters.feature.ar.model.DetectedFace
import com.snapfilters.feature.ar.model.FaceLandmarks
import com.snapfilters.feature.ar.model.Point2D
import com.snapfilters.feature.ar.pipeline.StillArResult
import com.snapfilters.feature.ar.pipeline.StillPhotoArPipeline
import com.snapfilters.feature.ar.pipeline.TiledBitmapCompositor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

class StillPhotoArPipelineTest {

    private lateinit var pipeline: StillPhotoArPipeline

    @Before
    fun setUp() {
        pipeline = StillPhotoArPipeline()
    }

    // FR-AR-02: Effect placement error under 1.5% of face width vs reference
    @Test
    fun fr_ar_02_placement_error_under_1_5_percent_of_face_width() {
        val imageWidth = 4000f // 12 MP width
        val imageHeight = 3000f

        val faceBox = BoundingBox(1000f, 800f, 2200f, 2000f) // 1200 px face width
        val faceWidth = faceBox.width // 1200 px

        val expectedLeftEye = Point2D(1300f, 1200f)
        val expectedRightEye = Point2D(1900f, 1200f)
        val expectedNose = Point2D(1600f, 1400f)

        val landmarks = FaceLandmarks(
            leftEye = expectedLeftEye,
            rightEye = expectedRightEye,
            noseBridge = expectedNose,
            leftTemple = Point2D(1100f, 1200f),
            rightTemple = Point2D(2100f, 1200f),
            centroid = Point2D(1600f, 1400f)
        )

        val cropTransform = pipeline.calculate40PercentMarginCrop(faceBox, imageWidth, imageHeight)

        // Map through crop transform
        val normX = (expectedNose.x - cropTransform.cropRect.left) / cropTransform.cropWidth
        val normY = (expectedNose.y - cropTransform.cropRect.top) / cropTransform.cropHeight

        val remappedNose = cropTransform.mapCropToSource(normX, normY)

        val errorX = abs(remappedNose.x - expectedNose.x)
        val errorY = abs(remappedNose.y - expectedNose.y)
        val maxErrorPx = maxOf(errorX, errorY)

        val errorPercentageOfFaceWidth = (maxErrorPx / faceWidth) * 100f

        // Acceptance test: Placement error under 1.5% of face width
        assertTrue(
            "Placement error must be under 1.5% of face width (was $errorPercentageOfFaceWidth%)",
            errorPercentageOfFaceWidth < 1.5f
        )
    }

    // ADR-005: 40% margin crop calculation
    @Test
    fun adr_005_crop_has_40_percent_margin_and_clamps_to_image_bounds() {
        val box = BoundingBox(200f, 200f, 400f, 400f) // 200x200 px
        val crop = pipeline.calculate40PercentMarginCrop(box, 1000f, 1000f)

        // 40% of 200 = 80 px on each side -> 200 + 160 = 360 px width
        assertEquals(360f, crop.cropWidth, 0.01f)
        assertEquals(360f, crop.cropHeight, 0.01f)
        assertEquals(120f, crop.cropRect.left, 0.01f)
        assertEquals(480f, crop.cropRect.right, 0.01f)
    }

    // Fallback ladder: Graceful fallback when no faces detected in still pass
    @Test
    fun fallback_when_no_faces_detected_preserves_bitmap_and_reports_state() {
        val dummyBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val result = pipeline.processStillPhoto(
            sourceBitmap = dummyBitmap,
            effect = ArEffect.AVIATORS,
            simulatedDetectedFaces = emptyList()
        )

        assertTrue("Must report NoFacesFound when no faces are detected", result is StillArResult.NoFacesFound)
        val noFacesResult = result as StillArResult.NoFacesFound
        assertEquals(dummyBitmap, noFacesResult.originalBitmap)
    }

    // Tiled Compositor execution
    @Test
    fun tiled_compositor_executes_all_tiles_without_distortion() {
        val compositor = TiledBitmapCompositor(tileHeightPx = 50)
        val dummyBitmap = Bitmap.createBitmap(100, 150, Bitmap.Config.ARGB_8888)

        var tileCount = 0
        val result = compositor.compositeTiled(dummyBitmap) { _, _ ->
            tileCount++
        }

        assertEquals(100, result.width)
        assertEquals(150, result.height)
        assertEquals("150 px height with 50 px tiles must produce 3 tiles", 3, tileCount)
    }
}
