package com.snapfilters.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Core network abstraction and Supabase session holder per ADR-002 and Chapter 11.
 */
class NetworkClient(
    val supabaseUrl: String = "https://placeholder.supabase.co",
    val anonKey: String = "placeholder-anon-key"
) {
    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    fun updateSessionToken(token: String?) {
        _accessToken.value = token
    }

    fun setNetworkStatus(online: Boolean) {
        _isConnected.value = online
    }
}
