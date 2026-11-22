package com.snapfilters.feature.sharing.coordinator

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.FileProvider
import com.snapfilters.core.common.result.AppResult
import com.snapfilters.feature.sharing.model.ShareCopyStrings
import com.snapfilters.feature.sharing.model.ShareMode
import java.io.File

/**
 * ShareCoordinator implements PRD v3.0 Chapter 17 specification:
 * - FR-SHR-01: Android Sharesheet for any compatible destination
 * - FR-SHR-02: Share Photo vs Share Original File as distinct actions
 * - FR-SHR-03: No false quality promises about third-party receivers
 * - Safe intent launching with PackageManager.resolveActivity verification
 */
class ShareCoordinator(
    private val context: Context,
    private val fileProviderAuthority: String = "${context.packageName}.fileprovider"
) {

    fun createShareIntent(file: File, shareMode: ShareMode): AppResult<Intent> {
        if (!file.exists()) {
            return AppResult.Error(
                NoSuchFileException(file),
                "Photograph file no longer exists on disk."
            )
        }

        val contentUri: Uri = try {
            FileProvider.getUriForFile(context, fileProviderAuthority, file)
        } catch (e: Exception) {
            return AppResult.Error(e, "Failed to generate secure content URI for sharing.")
        }

        val mimeType = when (shareMode) {
            ShareMode.PHOTO -> "image/jpeg"
            ShareMode.ORIGINAL_FILE -> resolveMimeType(file)
        }

        val chooserTitle = when (shareMode) {
            ShareMode.PHOTO -> "Share Photo (${ShareCopyStrings.PHOTO_EXPLANATION})"
            ShareMode.ORIGINAL_FILE -> "Share Original (${ShareCopyStrings.ORIGINAL_EXPLANATION})"
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(sendIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return AppResult.Success(chooser)
    }

    fun launchShare(file: File, shareMode: ShareMode): AppResult<Unit> {
        val intentResult = createShareIntent(file, shareMode)
        if (intentResult is AppResult.Error) {
            return intentResult
        }

        val intent = (intentResult as AppResult.Success).data
        val pm = context.packageManager
        val resolveInfo = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

        if (resolveInfo.isEmpty()) {
            return AppResult.Error(
                IllegalStateException("No compatible destination app found"),
                "No application found on device capable of receiving ${shareMode.displayName}."
            )
        }

        return try {
            context.startActivity(intent)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e, "Failed to launch system Sharesheet.")
        }
    }

    private fun resolveMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "png" -> "image/png"
            "heic" -> "image/heic"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
    }
}
