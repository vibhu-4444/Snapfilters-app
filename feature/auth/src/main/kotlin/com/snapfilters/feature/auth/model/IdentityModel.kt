package com.snapfilters.feature.auth.model

data class UserSession(
    val userId: String,
    val isAnonymous: Boolean = true,
    val displayName: String,
    val avatarPath: String? = null,
    val accessToken: String? = null, // Memory only, never stored on disk
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

data class RecoveryCode(
    val words: List<String>,
    val codeHashHex: String
)
