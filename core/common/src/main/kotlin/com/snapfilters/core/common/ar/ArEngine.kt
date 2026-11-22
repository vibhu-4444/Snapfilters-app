package com.snapfilters.core.common.ar

import android.graphics.Bitmap

/**
 * ArEngine interface published via core-common per PRD v3.0 Chapter 5 & 15.
 * Exposes live and still AR rendering pipelines.
 */
interface ArEngine {
    fun renderLiveFrame(frameBytes: ByteArray, width: Int, height: Int): Boolean
    fun renderStillImage(source: Bitmap, effectId: String): Bitmap
}
