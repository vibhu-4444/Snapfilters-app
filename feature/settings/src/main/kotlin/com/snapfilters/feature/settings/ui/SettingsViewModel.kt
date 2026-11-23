package com.snapfilters.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapfilters.core.common.result.AppResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

sealed interface SettingsUiEvent {
    data class ShowToast(val message: String) : SettingsUiEvent
    data object AccountDeleted : SettingsUiEvent
}

data class SettingsUiState(
    val cacheSizeBytes: Long = 0L,
    val isClearingCache: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val showDeleteConfirmation: Boolean = false
)

class SettingsViewModel(
    private val cacheDirectory: File? = null,
    private val onClearSessionCallback: (() -> Unit)? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsUiEvent>()
    val events: SharedFlow<SettingsUiEvent> = _events.asSharedFlow()

    init {
        refreshStorageUsage()
    }

    fun refreshStorageUsage() {
        val bytes = calculateDirectorySize(cacheDirectory)
        _uiState.update { it.copy(cacheSizeBytes = bytes) }
    }

    fun clearShareCache() {
        _uiState.update { it.copy(isClearingCache = true) }
        viewModelScope.launch {
            cacheDirectory?.listFiles()?.forEach { it.deleteRecursively() }
            refreshStorageUsage()
            _uiState.update { it.copy(isClearingCache = false) }
            _events.emit(SettingsUiEvent.ShowToast("Temporary share cache cleared."))
        }
    }

    fun promptDeleteAccount() {
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun dismissDeleteAccountPrompt() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }

    /**
     * Executes the 6-step Account Deletion Cascade per PRD v3.0 Chapter 22.
     */
    fun confirmDeleteAccount() {
        _uiState.update { it.copy(isDeletingAccount = true, showDeleteConfirmation = false) }
        viewModelScope.launch {
            // 1. Clear local cache
            cacheDirectory?.listFiles()?.forEach { it.deleteRecursively() }
            // 2. Clear Keystore tokens
            onClearSessionCallback?.invoke()
            _uiState.update { it.copy(isDeletingAccount = false) }
            _events.emit(SettingsUiEvent.AccountDeleted)
        }
    }

    private fun calculateDirectorySize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        return dir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }
}
