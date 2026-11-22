package com.snapfilters.feature.editor.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.snapfilters.core.common.filter.FilterEngine
import com.snapfilters.core.database.dao.PhotoDao
import com.snapfilters.core.database.entity.PhotoRecordEntity
import com.snapfilters.core.database.entity.SaveMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.UUID

sealed interface ExportResult {
    data class Success(val originalUri: String?, val filteredUri: String?) : ExportResult
    data class Error(val message: String) : ExportResult
}

/**
 * ImageExporter implements PRD v3.0 Chapter 16 export pipeline:
 * - 3 save modes: Original only, Filtered only, Both
 * - Preserves original bytes byte-for-byte in Original mode (verified via SHA-256)
 * - JPEG q95 by default
 * - Records non-destructive edit pairing in Room database
 */
class ImageExporter(
    private val context: Context,
    private val filterEngine: FilterEngine,
    private val photoDao: PhotoDao
) {

    suspend fun exportPhoto(
        sourceFile: File,
        filterId: String?,
        intensity: Float,
        saveMode: SaveMode
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            if (!sourceFile.exists()) {
                return@withContext ExportResult.Error("Source photograph not found.")
            }

            val captureId = UUID.randomUUID().toString()
            var savedOriginalUri: String? = null
            var savedFilteredUri: String? = null

            // 1. Original file save (Byte-for-byte copy, verified via SHA-256 per FR-EDT-03)
            if (saveMode == SaveMode.ORIGINAL_ONLY || saveMode == SaveMode.BOTH) {
                val originalBytes = sourceFile.readBytes()
                val originalSha256 = computeSha256(originalBytes)

                val uri = saveToMediaStore(
                    displayName = "SNAP_${System.currentTimeMillis()}_orig.jpg",
                    mimeType = "image/jpeg",
                    bytes = originalBytes
                ) ?: return@withContext ExportResult.Error("Failed to save original photo to MediaStore.")

                // Verify byte-for-byte identity
                val savedBytes = readUriBytes(uri)
                val savedSha256 = computeSha256(savedBytes)
                if (originalSha256 != savedSha256) {
                    return@withContext ExportResult.Error("Original file verification failed: byte mismatch.")
                }

                savedOriginalUri = uri.toString()
            }

            // 2. Filtered file save (Render LUT pass + JPEG q95)
            if (saveMode == SaveMode.FILTERED_ONLY || saveMode == SaveMode.BOTH) {
                val sourceBitmap = BitmapFactory.decodeFile(sourceFile.absolutePath)
                    ?: return@withContext ExportResult.Error("Failed to decode source image.")

                val renderedBitmap = if (filterId != null && intensity > 0.0f) {
                    filterEngine.apply(filterId, intensity, sourceBitmap)
                } else {
                    sourceBitmap
                }

                val outStream = java.io.ByteArrayOutputStream()
                renderedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outStream)
                val renderedBytes = outStream.toByteArray()

                val uri = saveToMediaStore(
                    displayName = "SNAP_${System.currentTimeMillis()}_cinematic.jpg",
                    mimeType = "image/jpeg",
                    bytes = renderedBytes
                ) ?: return@withContext ExportResult.Error("Failed to save filtered photo to MediaStore.")

                savedFilteredUri = uri.toString()
            }

            // 3. Record in Room database for non-destructive re-editing
            val record = PhotoRecordEntity(
                captureId = captureId,
                mode = saveMode,
                originalUri = savedOriginalUri,
                editedUri = savedFilteredUri,
                filterId = filterId,
                intensity = intensity
            )
            photoDao.insertRecord(record)

            ExportResult.Success(savedOriginalUri, savedFilteredUri)
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "Unknown export error")
        }
    }

    private fun saveToMediaStore(displayName: String, mimeType: String, bytes: ByteArray): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Snapfilters")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: return null

        resolver.openOutputStream(uri)?.use { stream ->
            stream.write(bytes)
            stream.flush()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }

        return uri
    }

    private fun readUriBytes(uri: Uri): ByteArray {
        val resolver = context.contentResolver
        return resolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
    }

    private fun computeSha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(bytes).joinToString("") { "%02x".format(it) }
    }
}
