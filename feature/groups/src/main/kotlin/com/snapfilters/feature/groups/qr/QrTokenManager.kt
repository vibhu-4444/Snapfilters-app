package com.snapfilters.feature.groups.qr

import android.net.Uri
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * QrTokenManager implements PRD v3.0 Chapter 11 (ADR-010):
 * - 32-byte CSPRNG random token -> 256 bits of entropy
 * - Base64URL encoding (43 characters)
 * - SHA-256 hashed at rest
 * - QR carries token only, NEVER group ID or database keys
 */
object QrTokenManager {

    private const val DOMAIN_PREFIX = "https://app.snapfilters.local/join"
    private val secureRandom = SecureRandom()

    fun mintToken(): String {
        val randomBytes = ByteArray(32)
        secureRandom.nextBytes(randomBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
    }

    fun buildQrPayload(token: String): String {
        return "$DOMAIN_PREFIX?t=$token"
    }

    fun parseTokenFromUri(uriString: String): String? {
        val uri = try {
            Uri.parse(uriString)
        } catch (e: Exception) {
            return null
        }
        return uri.getQueryParameter("t")
    }

    fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(token.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
