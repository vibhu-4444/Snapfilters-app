package com.snapfilters.feature.groups.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapfilters.core.common.result.AppResult
import com.snapfilters.feature.groups.model.Group
import com.snapfilters.feature.groups.model.JoiningPolicy
import com.snapfilters.feature.groups.model.RedeemResult
import com.snapfilters.feature.groups.repository.GroupRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface GroupUiEvent {
    data class ShowToast(val message: String) : GroupUiEvent
    data class GroupCreated(val group: Group) : GroupUiEvent
    data class QrReady(val payload: String) : GroupUiEvent
}

data class GroupsUiState(
    val groups: List<Group> = emptyList(),
    val showCreateDialog: Boolean = false,
    val showJoinPreview: Boolean = false,
    val currentRedeemResult: RedeemResult? = null,
    val activeQrPayload: String? = null
)

class GroupsViewModel(
    private val groupRepository: GroupRepository,
    private val currentUserId: String,
    private val currentUserDisplayName: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GroupUiEvent>()
    val events: SharedFlow<GroupUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            groupRepository.userGroups.collect { groupList ->
                _uiState.update { it.copy(groups = groupList) }
            }
        }
    }

    fun openCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun dismissCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun createGroup(name: String, description: String?, policy: JoiningPolicy) {
        viewModelScope.launch {
            when (val result = groupRepository.createGroup(name, description, currentUserId, policy)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(showCreateDialog = false) }
                    _events.emit(GroupUiEvent.GroupCreated(result.data))
                }
                is AppResult.Error -> {
                    _events.emit(GroupUiEvent.ShowToast(result.message))
                }
                AppResult.Loading -> Unit
            }
        }
    }

    fun mintInviteQr(groupId: String) {
        viewModelScope.launch {
            when (val result = groupRepository.mintInvitation(groupId, currentUserId)) {
                is AppResult.Success -> {
                    val payload = result.data.first
                    _uiState.update { it.copy(activeQrPayload = payload) }
                    _events.emit(GroupUiEvent.QrReady(payload))
                }
                is AppResult.Error -> {
                    _events.emit(GroupUiEvent.ShowToast(result.message))
                }
                AppResult.Loading -> Unit
            }
        }
    }

    fun onQrScanned(rawToken: String) {
        viewModelScope.launch {
            val result = groupRepository.redeemInvitation(rawToken, currentUserId, currentUserDisplayName)
            _uiState.update {
                it.copy(
                    showJoinPreview = true,
                    currentRedeemResult = result
                )
            }
        }
    }

    fun dismissJoinPreview() {
        _uiState.update { it.copy(showJoinPreview = false, currentRedeemResult = null) }
    }
}
