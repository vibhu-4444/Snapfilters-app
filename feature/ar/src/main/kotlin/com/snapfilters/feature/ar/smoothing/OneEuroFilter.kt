package com.snapfilters.feature.ar.smoothing

import kotlin.math.PI

/**
 * One Euro Filter implementation per PRD v3.0 Chapter 15:
 * Precision temporal smoothing for landmark coordinates.
 * Heavy smoothing on slow motion to eliminate jitter, zero lag on fast head rotation.
 * Parameters: minCutoff = 1.0 Hz, beta = 0.007
 */
class OneEuroFilter(
    private val minCutoff: Double = 1.0,
    private val beta: Double = 0.007,
    private val dCutoff: Double = 1.0
) {
    private var xPrev: Double? = null
    private var dxPrev: Double = 0.0
    private var tPrev: Long? = null

    fun filter(x: Double, timestampMillis: Long): Double {
        if (xPrev == null || tPrev == null) {
            xPrev = x
            tPrev = timestampMillis
            return x
        }

        val dt = (timestampMillis - tPrev!!).toDouble() / 1000.0
        tPrev = timestampMillis
        if (dt <= 0.0) return xPrev!!

        // Compute derivative
        val dx = (x - xPrev!!) / dt
        val edx = exponentialSmoothing(dx, dxPrev, alpha(dt, dCutoff))
        dxPrev = edx

        // Cutoff frequency adapts with speed
        val cutoff = minCutoff + beta * kotlin.math.abs(edx)
        val filteredX = exponentialSmoothing(x, xPrev!!, alpha(dt, cutoff))
        xPrev = filteredX

        return filteredX
    }

    private fun alpha(dt: Double, cutoff: Double): Double {
        val tau = 1.0 / (2.0 * PI * cutoff)
        return 1.0 / (1.0 + tau / dt)
    }

    private fun exponentialSmoothing(curr: Double, prev: Double, alpha: Double): Double {
        return alpha * curr + (1.0 - alpha) * prev
    }

    fun reset() {
        xPrev = null
        dxPrev = 0.0
        tPrev = null
    }
}

class LandmarkSmoother {
    private val xFilter = OneEuroFilter()
    private val yFilter = OneEuroFilter()

    fun smooth(x: Float, y: Float, timestampMillis: Long): Pair<Float, Float> {
        val sx = xFilter.filter(x.toDouble(), timestampMillis).toFloat()
        val sy = yFilter.filter(y.toDouble(), timestampMillis).toFloat()
        return Pair(sx, sy)
    }

    fun reset() {
        xFilter.reset()
        yFilter.reset()
    }
}
