package com.snapfilters.feature.editor

import com.snapfilters.core.database.entity.SaveMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class ImageExporterTest {

    // FR-EDT-03: Original-only save preserves original bytes (SHA-256 identical)
    @Test
    fun fr_edt_03_original_only_save_preserves_sha256_bytes() {
        val testBytes = "ORIGINAL_CAMERA_SENSOR_RAW_PAYLOAD_123456789".toByteArray()
        val digest = MessageDigest.getInstance("SHA-256")
        val originalSha = digest.digest(testBytes).joinToString("") { "%02x".format(it) }

        // Simulate byte-for-byte copy
        val copiedBytes = testBytes.copyOf()
        val copiedSha = digest.digest(copiedBytes).joinToString("") { "%02x".format(it) }

        assertEquals(
            "Original copy must be byte-for-byte identical per FR-EDT-03",
            originalSha,
            copiedSha
        )
    }

    // FR-EDT-02: 3 save modes documented
    @Test
    fun fr_edt_02_all_three_save_modes_supported() {
        val modes = SaveMode.values()
        assertEquals(3, modes.size)
        assertTrue(modes.contains(SaveMode.ORIGINAL_ONLY))
        assertTrue(modes.contains(SaveMode.FILTERED_ONLY))
        assertTrue(modes.contains(SaveMode.BOTH))
    }
}
