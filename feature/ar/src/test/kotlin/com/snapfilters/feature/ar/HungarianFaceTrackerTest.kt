package com.snapfilters.feature.ar

import com.snapfilters.feature.ar.model.BoundingBox
import com.snapfilters.feature.ar.model.DetectedFace
import com.snapfilters.feature.ar.model.DeviceArTier
import com.snapfilters.feature.ar.model.FaceLandmarks
import com.snapfilters.feature.ar.model.Point2D
import com.snapfilters.feature.ar.smoothing.OneEuroFilter
import com.snapfilters.feature.ar.tracker.HungarianFaceTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HungarianFaceTrackerTest {

    private lateinit var tracker: HungarianFaceTracker

    @Before
    fun setUp() {
        tracker = HungarianFaceTracker()
    }

    private fun createFace(cx: Float, cy: Float, size: Float = 100f): DetectedFace {
        val half = size / 2f
        return DetectedFace(
            boundingBox = BoundingBox(cx - half, cy - half, cx + half, cy + half),
            landmarks = FaceLandmarks(
                leftEye = Point2D(cx - 20f, cy - 10f),
                rightEye = Point2D(cx + 20f, cy - 10f),
                noseBridge = Point2D(cx, cy),
                leftTemple = Point2D(cx - 40f, cy),
                rightTemple = Point2D(cx + 40f, cy),
                centroid = Point2D(cx, cy)
            )
        )
    }

    // FR-AR-03: Effects must NOT swap between faces across frames
    @Test
    fun fr_ar_03_no_effect_swap_when_detector_reorders_faces() {
        val faceA = createFace(100f, 200f)
        val faceB = createFace(300f, 200f)

        // Frame 1: [A, B] -> Spawns 2 tracks
        tracker.updateTracks(listOf(faceA, faceB), 1000f, 1000L)

        // Frame 2: Stabilize tracks (hit count = 2)
        val tracksFrame2 = tracker.updateTracks(listOf(faceA, faceB), 1000f, 1033L)
        assertEquals(2, tracksFrame2.size)
        val trackIdA = tracksFrame2.find { it.face.landmarks.centroid.x < 200f }!!.trackId
        val trackIdB = tracksFrame2.find { it.face.landmarks.centroid.x > 200f }!!.trackId
        assertNotEquals(trackIdA, trackIdB)

        // Frame 3: Detector swaps array order: [B, A]
        val tracksFrame3 = tracker.updateTracks(listOf(faceB, faceA), 1000f, 1066L)
        assertEquals(2, tracksFrame3.size)

        // Verify ID for face at X ~ 100 is still trackIdA, and X ~ 300 is trackIdB
        val trackAAfterSwap = tracksFrame3.find { it.face.landmarks.centroid.x < 200f }!!
        val trackBAfterSwap = tracksFrame3.find { it.face.landmarks.centroid.x > 200f }!!

        assertEquals("Face at x=100 must retain trackIdA despite detector reordering", trackIdA, trackAAfterSwap.trackId)
        assertEquals("Face at x=300 must retain trackIdB despite detector reordering", trackIdB, trackBAfterSwap.trackId)
    }

    // Guard 1: Jump > 40% frame width holds previous pose (ADR-004)
    @Test
    fun jump_guard_holds_previous_pose_when_detection_jumps_abnormally() {
        val face = createFace(100f, 200f)
        tracker.updateTracks(listOf(face), 1000f, 1000L)
        tracker.updateTracks(listOf(face), 1000f, 1033L)

        // Sudden jump of 500 px (50% of 1000 px frame width)
        val jumpedFace = createFace(600f, 200f)
        val tracks = tracker.updateTracks(listOf(jumpedFace), 1000f, 1066L)

        assertEquals(1, tracks.size)
        val track = tracks.first()
        // Must hold last pose (around x=100) instead of jumping immediately to 600
        assertTrue("Track must hold last pose during sudden jump", track.face.landmarks.centroid.x < 300f)
    }

    // Guard 2: False positive guard - requires 2 consecutive frames
    @Test
    fun new_track_requires_two_consecutive_frames_to_activate() {
        val face = createFace(200f, 200f)

        // Frame 1: First appearance (hit count = 1) -> Filtered out
        val frame1Tracks = tracker.updateTracks(listOf(face), 1000f, 1000L)
        assertTrue("New track must not show effect on frame 1 to avoid flashing", frame1Tracks.isEmpty())

        // Frame 2: Confirmed appearance (hit count = 2) -> Active
        val frame2Tracks = tracker.updateTracks(listOf(face), 1000f, 1033L)
        assertEquals(1, frame2Tracks.size)
    }

    // Miss Counter: Track survives up to 15 frames before deletion
    @Test
    fun track_survives_temporary_miss_and_drops_after_15_frames() {
        val face = createFace(200f, 200f)
        tracker.updateTracks(listOf(face), 1000f, 1000L)
        tracker.updateTracks(listOf(face), 1000f, 1033L)

        // Simulate 10 frames of missed face (e.g. blink or hand pass)
        for (i in 1..10) {
            tracker.updateTracks(emptyList(), 1000f, 1033L + i * 33L)
        }

        // Face reappears on frame 11 -> Should match existing track
        val tracksReappeared = tracker.updateTracks(listOf(face), 1000f, 1500L)
        assertEquals("Track must survive temporary 10-frame occlusions", 1, tracksReappeared.size)

        // Simulate 20 frames of continuous absence -> Must be dropped
        for (i in 1..20) {
            tracker.updateTracks(emptyList(), 1000f, 1500L + i * 33L)
        }
        val tracksAfterLongAbsence = tracker.updateTracks(listOf(face), 1000f, 2500L)
        // Since previous track was dropped, new detection starts fresh at hitCount = 1 (filtered out on 1st frame)
        assertTrue(tracksAfterLongAbsence.isEmpty())
    }

    // One Euro Filter tests
    @Test
    fun one_euro_filter_dampens_jitter_at_zero_velocity() {
        val filter = OneEuroFilter(minCutoff = 1.0, beta = 0.007)
        filter.filter(100.0, 1000L)

        // Small subpixel jitter (+0.5, -0.5)
        val out1 = filter.filter(100.5, 1033L)
        val out2 = filter.filter(99.5, 1066L)

        // Out values should be heavily smoothed toward 100.0
        assertTrue(out1 < 100.3)
        assertTrue(out2 > 99.7)
    }

    // Device tiering validation
    @Test
    fun device_tiering_limits_match_prd_specification() {
        assertEquals("Tier A must support 4 faces", 4, DeviceArTier.TIER_A.maxLiveFaces)
        assertEquals("Tier B must support 3 faces", 3, DeviceArTier.TIER_B.maxLiveFaces)
        assertEquals("Tier C must support 2 faces", 2, DeviceArTier.TIER_C.maxLiveFaces)
    }
}
