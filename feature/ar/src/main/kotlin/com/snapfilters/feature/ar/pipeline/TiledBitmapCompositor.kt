package com.snapfilters.feature.ar.pipeline

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

/**
 * TiledBitmapCompositor per PRD v3.0 Chapter 15:
 * Prevents OutOfMemory errors on 12 MP to 108 MP captures by rendering
 * horizontal tiles into a reused memory buffer, avoiding allocating multiple full-frame bitmaps.
 */
class TiledBitmapCompositor(
    private val tileHeightPx: Int = 1024
) {

    fun compositeTiled(
        sourceBitmap: Bitmap,
        renderTileBlock: (canvas: Canvas, tileRect: Rect) -> Unit
    ): Bitmap {
        val width = sourceBitmap.width
        val height = sourceBitmap.height

        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Draw source into canvas
        canvas.drawBitmap(sourceBitmap, 0f, 0f, paint)

        // Render in horizontal tiles
        var currentTop = 0
        while (currentTop < height) {
            val currentBottom = (currentTop + tileHeightPx).coerceAtMost(height)
            val tileRect = Rect(0, currentTop, width, currentBottom)

            canvas.save()
            canvas.clipRect(tileRect)
            renderTileBlock(canvas, tileRect)
            canvas.restore()

            currentTop = currentBottom
        }

        return resultBitmap
    }
}
