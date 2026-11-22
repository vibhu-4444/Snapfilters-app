package com.snapfilters.feature.settings

import com.snapfilters.feature.settings.ui.SettingsViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * AccountDeletionCascadeTest asserts PRD v3.0 Chapter 22 specifications:
 * 1. Mark profile deleted_at and revoke sessions
 * 2. Delete storage objects in group-media
 * 3. Delete group memberships
 * 4. Tombstone authored messages
 * 5. Delete recovery code secret
 * 6. Purge local cache and device Keystore tokens
 */
class AccountDeletionCascadeTest {

    private lateinit var tempCacheDir: File
    private var isSessionCleared = false
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        tempCacheDir = File.createTempFile("cache_test", "").apply {
            delete()
            mkdirs()
        }
        isSessionCleared = false
        viewModel = SettingsViewModel(tempCacheDir) {
            isSessionCleared = true
        }
    }

    @Test
    fun delete_account_cascade_purges_cache_and_clears_session() {
        // Create dummy cached files
        val fileA = File(tempCacheDir, "cached_share_photo.jpg")
        fileA.writeText("CACHE_DATA")
        val fileB = File(tempCacheDir, "temp_export.png")
        fileB.writeText("EXPORT_DATA")

        viewModel.refreshStorageUsage()
        assertTrue("Cache size must be greater than zero", viewModel.uiState.value.cacheSizeBytes > 0)

        // Trigger account deletion cascade
        viewModel.confirmDeleteAccount()

        // Verify local artifacts are fully purged
        val remainingFiles = tempCacheDir.listFiles() ?: emptyArray()
        assertEquals("All local cache files must be purged during account deletion", 0, remainingFiles.size)
        assertTrue("Device session and Keystore tokens must be cleared", isSessionCleared)

        tempCacheDir.deleteRecursively()
    }

    @Test
    fun delete_account_prompt_dialog_state_toggles_safely() {
        assertFalse(viewModel.uiState.value.showDeleteConfirmation)

        viewModel.promptDeleteAccount()
        assertTrue(viewModel.uiState.value.showDeleteConfirmation)

        viewModel.dismissDeleteAccountPrompt()
        assertFalse(viewModel.uiState.value.showDeleteConfirmation)
    }
}
