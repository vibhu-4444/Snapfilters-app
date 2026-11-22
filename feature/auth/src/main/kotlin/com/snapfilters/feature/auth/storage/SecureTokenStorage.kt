package com.snapfilters.feature.auth.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * SecureTokenStorage stores device-bound refresh tokens and identity metadata
 * in EncryptedSharedPreferences backed by Android Keystore (AES-256-GCM) per ADR-003.
 */
class SecureTokenStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "snap_secure_session_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun saveUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun saveDisplayName(name: String) {
        prefs.edit().putString(KEY_DISPLAY_NAME, name).apply()
    }

    fun getDisplayName(): String? {
        return prefs.getString(KEY_DISPLAY_NAME, null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_REFRESH_TOKEN = "snap_refresh_token"
        private const val KEY_USER_ID = "snap_user_id"
        private const val KEY_DISPLAY_NAME = "snap_display_name"
    }
}
