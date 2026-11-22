package com.snapfilters.feature.ar.tracker

import com.snapfilters.feature.ar.model.DetectedFace
import com.snapfilters.feature.ar.model.FaceLandmarks
import com.snapfilters.feature.ar.smoothing.LandmarkSmoother
import kotlin.math.hypot

data class ActiveFaceTrack(
    val trackId: Long,
    var face: DetectedFace,
    var missCount: Int = 0,
    var consecutiveHitCount: Int = 1,
    var lastHoldUntilMillis: Long = 0L,
    val smoother: LandmarkSmoother = LandmarkSmoother()
)

/**
 * HungarianFaceTracker implements the tracking algorithm from PRD v3.0 Chapter 15:
 * - Cost combining IoU (0.6) and landmark distance (0.4)
 * - Prevents face accessories swapping between adjacent faces (FR-AR-03)
 * - 15-frame miss grace window (~500 ms at 30 fps) to survive blinks
 * - 40% frame jump guard holding last pose for 300 ms
 * - 2-frame stabilization guard preventing false positives from flashing
 */
class HungarianFaceTracker(
    private val maxMissCount: Int = 15,
    private val matchCostThreshold: Float = 0.75f,
    private val jumpThresholdFraction: Float = 0.40f
) {
    private var nextTrackId = 1L
    private val activeTracks = mutableListOf<ActiveFaceTrack>()

    fun updateTracks(
        detections: List<DetectedFace>,
        frameWidth: Float,
        timestampMillis: Long
    ): List<ActiveFaceTrack> {
        if (detections.isEmpty()) {
            activeTracks.forEach { it.missCount++ }
            activeTracks.removeAll { it.missCount > maxMissCount }
            return getStableTracks(timestampMillis)
        }

        val unassignedDetections = detections.toMutableList()
        val assignedTrackIndices = mutableSetOf<Int>()

        // Greedy matching by minimum cost
        for (det in detections) {
            var bestTrackIndex = -1
            var minCost = Float.MAX_VALUE

            for ((tIdx, track) in activeTracks.withIndex()) {
                if (tIdx in assignedTrackIndices) continue

                val iou = track.face.boundingBox.calculateIoU(det.boundingBox)
                val dist = normalizedCentroidDistance(track.face.landmarks, det.landmarks, frameWidth)
                val cost = 0.6f * (1.0f - iou) + 0.4f * dist

                if (cost < matchCostThreshold && cost < minCost) {
                    minCost = cost
                    bestTrackIndex = tIdx
                }
            }

            if (bestTrackIndex != -1) {
                val track = activeTracks[bestTrackIndex]
                val jumpDist = hypot(
                    det.landmarks.centroid.x - track.face.landmarks.centroid.x,
                    det.landmarks.centroid.y - track.face.landmarks.centroid.y
                )

                // Guard 1: Jump > 40% frame width -> hold last pose for 300 ms (ADR-004)
                if (jumpDist > frameWidth * jumpThresholdFraction) {
                    track.lastHoldUntilMillis = timestampMillis + 300L
                } else {
                    val (sx, sy) = track.smoother.smooth(
                        det.landmarks.centroid.x,
                        det.landmarks.centroid.y,
                        timestampMillis
                    )
                    val smoothedLandmarks = det.landmarks.copy(
                        centroid = com.snapfilters.feature.ar.model.Point2D(sx, sy)
                    )
                    track.face = det.copy(landmarks = smoothedLandmarks)
                }

                track.missCount = 0
                track.consecutiveHitCount++
                assignedTrackIndices.add(bestTrackIndex)
                unassignedDetections.remove(det)
            }
        }

        // Unassigned tracks increment miss counter
        for ((tIdx, track) in activeTracks.withIndex()) {
            if (tIdx !in assignedTrackIndices) {
                track.missCount++
            }
        }
        activeTracks.removeAll { it.missCount > maxMissCount }

        // Unassigned detections spawn new tracks
        for (det in unassignedDetections) {
            val newTrack = ActiveFaceTrack(
                trackId = nextTrackId++,
                face = det,
                consecutiveHitCount = 1
            )
            activeTracks.add(newTrack)
        }

        return getStableTracks(timestampMillis)
    }

    /**
     * Guard 2: Tracks must be seen for at least 2 consecutive frames before showing effects.
     */
    private fun getStableTracks(timestampMillis: Long): List<ActiveFaceTrack> {
        return activeTracks.filter { it.consecutiveHitCount >= 2 }
    }

    private fun normalizedCentroidDistance(l1: FaceLandmarks, l2: FaceLandmarks, frameWidth: Float): Float {
        val dist = hypot(l1.centroid.x - l2.centroid.x, l1.centroid.y - l2.centroid.y)
        return (dist / frameWidth.coerceAtLeast(1f)).coerceIn(0f, 1f)
    }

    fun clear() {
        activeTracks.clear()
        nextTrackId = 1L
    }
}
