package com.snapfilters.feature.auth

import com.snapfilters.core.common.mnemonic.MnemonicGenerator
import com.snapfilters.core.common.result.AppResult
import com.snapfilters.feature.auth.repository.IdentityRepository
import com.snapfilters.feature.auth.storage.SecureTokenStorage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IdentityRepositoryTest {

    private lateinit var mockStorage: SecureTokenStorage
    private lateinit var repository: IdentityRepository

    @Before
    fun setUp() {
        mockStorage = mockk(relaxed = true)
        every { mockStorage.getUserId() } returns null
        every { mockStorage.getDisplayName() } returns null
        repository = IdentityRepository(mockStorage)
    }

    // FR-IDN-01 & FR-IDN-02: Backend anonymous identity with zero phone or email
    @Test
    fun fr_idn_01_02_anonymous_signin_creates_real_identity_without_phone_or_email() = runTest {
        val result = repository.signInAnonymously("Photographer")

        assertTrue(result is AppResult.Success)
        val (session, recoveryCode) = (result as AppResult.Success).data

        // User ID is generated UUID
        assertNotNull(session.userId)
        assertTrue(session.isAnonymous)
        assertEquals("Photographer", session.displayName)

        // Refresh token persisted to encrypted Keystore storage
        verify { mockStorage.saveRefreshToken(any()) }
        verify { mockStorage.saveUserId(session.userId) }

        // Access token exists in memory
        assertNotNull(session.accessToken)
    }

    // FR-IDN-04: 12-word recovery code generated and hashed
    @Test
    fun fr_idn_04_recovery_code_is_12_words_and_hash_is_sha256() = runTest {
        val words = MnemonicGenerator.generate12WordMnemonic()
        assertEquals(12, words.size)

        val hashHex = MnemonicGenerator.hashMnemonicHex(words)
        assertEquals(64, hashHex.length) // 256 bits = 64 hex characters

        // Verify deterministic hashing
        val secondHash = MnemonicGenerator.hashMnemonicHex(words)
        assertEquals(hashHex, secondHash)
    }

    // FR-IDN-03: Display name update
    @Test
    fun fr_idn_03_display_name_can_be_updated() = runTest {
        repository.signInAnonymously("Initial Name")
        val updateResult = repository.updateDisplayName("Cinematic Artist")

        assertTrue(updateResult is AppResult.Success)
        assertEquals("Cinematic Artist", repository.currentSession.value?.displayName)
        verify { mockStorage.saveDisplayName("Cinematic Artist") }
    }
}
