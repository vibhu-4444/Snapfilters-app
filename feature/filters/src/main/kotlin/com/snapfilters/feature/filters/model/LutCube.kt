package com.snapfilters.feature.filters.model

/**
 * LutCube represents a parsed 3D Color Lookup Table.
 * Storage: Flat FloatArray with size^3 * 3 entries (R, G, B floats).
 */
data class LutCube(
    val title: String,
    val size: Int, // 17, 33, or 65
    val domainMin: FloatArray = floatArrayOf(0f, 0f, 0f),
    val domainMax: FloatArray = floatArrayOf(1f, 1f, 1f),
    val data: FloatArray
) {
    init {
        require(size in setOf(17, 33, 65)) { "LUT size must be 17, 33, or 65" }
        require(data.size == size * size * size * 3) {
            "Data size mismatch: expected ${size * size * size * 3}, got ${data.size}"
        }
    }

    /**
     * Software trilinear interpolation sampler matching OpenGL ES 3.0 texture(uLut, coord).
     * Used for CPU export pass and mathematical verification.
     */
    fun sample(r: Float, g: Float, b: Float): Triple<Float, Float, Float> {
        val rNorm = ((r - domainMin[0]) / (domainMax[0] - domainMin[0])).coerceIn(0f, 1f)
        val gNorm = ((g - domainMin[1]) / (domainMax[1] - domainMin[1])).coerceIn(0f, 1f)
        val bNorm = ((b - domainMin[2]) / (domainMax[2] - domainMin[2])).coerceIn(0f, 1f)

        // Scale and offset matching shader contract: coord * ((size - 1) / size) + 0.5 / size
        val maxIndex = size - 1
        val rIdx = rNorm * maxIndex
        val gIdx = gNorm * maxIndex
        val bIdx = bNorm * maxIndex

        val r0 = rIdx.toInt().coerceIn(0, maxIndex - 1)
        val r1 = (r0 + 1).coerceAtMost(maxIndex)
        val g0 = gIdx.toInt().coerceIn(0, maxIndex - 1)
        val g1 = (g0 + 1).coerceAtMost(maxIndex)
        val b0 = bIdx.toInt().coerceIn(0, maxIndex - 1)
        val b1 = (b0 + 1).coerceAtMost(maxIndex)

        val rFrac = rIdx - r0
        val gFrac = gIdx - g0
        val bFrac = bIdx - b0

        fun getRgb(ri: Int, gi: Int, bi: Int): FloatArray {
            val index = (bi * size * size + gi * size + ri) * 3
            return floatArrayOf(data[index], data[index + 1], data[index + 2])
        }

        val c000 = getRgb(r0, g0, b0)
        val c100 = getRgb(r1, g0, b0)
        val c010 = getRgb(r0, g1, b0)
        val c110 = getRgb(r1, g1, b0)
        val c001 = getRgb(r0, g0, b1)
        val c101 = getRgb(r1, g0, b1)
        val c011 = getRgb(r0, g1, b1)
        val c111 = getRgb(r1, g1, b1)

        val outR = lerp(
            lerp(lerp(c000[0], c100[0], rFrac), lerp(c010[0], c110[0], rFrac), gFrac),
            lerp(lerp(c001[0], c101[0], rFrac), lerp(c011[0], c111[0], rFrac), gFrac),
            bFrac
        )
        val outG = lerp(
            lerp(lerp(c000[1], c100[1], rFrac), lerp(c010[1], c110[1], rFrac), gFrac),
            lerp(lerp(c001[1], c101[1], rFrac), lerp(c011[1], c111[1], rFrac), gFrac),
            bFrac
        )
        val outB = lerp(
            lerp(lerp(c000[2], c100[2], rFrac), lerp(c010[2], c110[2], rFrac), gFrac),
            lerp(lerp(c001[2], c101[2], rFrac), lerp(c011[2], c111[2], rFrac), gFrac),
            bFrac
        )

        return Triple(outR, outG, outB)
    }

    private fun lerp(a: Float, b: Float, t: Float): Float = a + t * (b - a)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LutCube
        return title == other.title && size == other.size && data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + size
        result = 31 * result + data.contentHashCode()
        return result
    }
}
