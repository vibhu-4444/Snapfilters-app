package com.snapfilters.feature.sharing

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.snapfilters.core.common.result.AppResult
import com.snapfilters.feature.sharing.coordinator.ShareCoordinator
import com.snapfilters.feature.sharing.model.ShareCopyStrings
import com.snapfilters.feature.sharing.model.ShareMode
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class ShareCoordinatorTest {

    private lateinit var mockContext: Context
    private lateinit var coordinator: ShareCoordinator
    private val testAuthority = "com.snapfilters.fileprovider"

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        every { mockContext.packageName } returns "com.snapfilters"
        coordinator = ShareCoordinator(mockContext, testAuthority)
    }

    // FR-SHR-03: Assert forbidden marketing claims do NOT exist in copy
    @Test
    fun fr_shr_03_assert_no_misleading_whatsapp_claims() {
        val forbidden = ShareCopyStrings.FORBIDDEN_CLAIMS
        assertTrue(forbidden.contains("Lossless sharing to WhatsApp"))
        assertTrue(forbidden.contains("WhatsApp keeps your original quality"))
        assertTrue(forbidden.contains("Sent in HD"))

        // Approved copy explicitly mentions potential receiver compression
        assertEquals("Photo: optimised for chat apps.", ShareCopyStrings.PHOTO_EXPLANATION)
        assertEquals("The receiving app may compress this image.", ShareCopyStrings.RECEIVER_DISCLAIMER)
    }

    // FR-SHR-02: Distinct share modes produce distinct MIME types
    @Test
    fun fr_shr_02_distinct_share_modes_produce_correct_mime_and_flags() {
        val tempFile = File.createTempFile("test_photo", ".png")
        tempFile.writeText("PNG_DATA")

        val dummyUri = Uri.parse("content://com.snapfilters.fileprovider/media/test_photo.png")
        mockkStatic(FileProvider::class)
        every { FileProvider.getUriForFile(any(), any(), any()) } returns dummyUri

        // 1. Share Photo -> forces image/jpeg
        val photoResult = coordinator.createShareIntent(tempFile, ShareMode.PHOTO)
        assertTrue(photoResult is AppResult.Success)
        val photoChooser = (photoResult as AppResult.Success).data
        val photoSendIntent = photoChooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)

        assertEquals("image/jpeg", photoSendIntent?.type)
        val hasPhotoGrant = (photoSendIntent?.flags ?: 0) and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0
        assertTrue("Intent must grant read URI permission", hasPhotoGrant)

        // 2. Share Original File -> preserves source MIME (image/png)
        val origResult = coordinator.createShareIntent(tempFile, ShareMode.ORIGINAL_FILE)
        assertTrue(origResult is AppResult.Success)
        val origChooser = (origResult as AppResult.Success).data
        val origSendIntent = origChooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)

        assertEquals("image/png", origSendIntent?.type)
        val hasOrigGrant = (origSendIntent?.flags ?: 0) and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0
        assertTrue("Intent must grant read URI permission", hasOrigGrant)

        tempFile.delete()
    }

    // Error handling: Missing file returns clear error without crash
    @Test
    fun missing_file_returns_error_result() {
        val nonExistent = File("/path/to/missing_file.jpg")
        val result = coordinator.createShareIntent(nonExistent, ShareMode.PHOTO)

        assertTrue(result is AppResult.Error)
        assertEquals("Photograph file no longer exists on disk.", (result as AppResult.Error).message)
    }
}
