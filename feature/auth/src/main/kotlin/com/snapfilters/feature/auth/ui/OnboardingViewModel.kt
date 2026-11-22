package com.snapfilters.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapfilters.core.common.result.AppResult
import com.snapfilters.feature.auth.model.RecoveryCode
import com.snapfilters.feature.auth.repository.IdentityRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface OnboardingUiEvent {
    data object OnboardingSuccess : OnboardingUiEvent()
    data class ShowError(val message: String) : OnboardingUiEvent()
}

data class OnboardingUiState(
    val displayNameInput: String = "",
    val isLoading: Boolean = false,
    val recoveryCode: RecoveryCode? = null,
    val isRecoveryCodeConfirmed: Boolean = false
)

class OnboardingViewModel(
    private val identityRepository: IdentityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<OnboardingUiEvent>()
    val events: SharedFlow<OnboardingUiEvent> = _events.asSharedFlow()

    fun onDisplayNameChanged(name: String) {
        _uiState.update { it.copy(displayNameInput = name.take(40)) }
    }

    fun submitDisplayName() {
        val name = _uiState.value.displayNameInput.trim()
        if (name.isEmpty()) {
            viewModelScope.launch {
                _events.emit(OnboardingUiEvent.ShowError("Please enter a display name."))
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = identityRepository.signInAnonymously(name)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            recoveryCode = result.data.second
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(OnboardingUiEvent.ShowError(result.message))
                }
                AppResult.Loading -> Unit
            }
        }
    }

    fun confirmRecoveryCodeSaved() {
        _uiState.update { it.copy(isRecoveryCodeConfirmed = true) }
        viewModelScope.launch {
            _events.emit(OnboardingUiEvent.OnboardingSuccess)
        }
    }
}
