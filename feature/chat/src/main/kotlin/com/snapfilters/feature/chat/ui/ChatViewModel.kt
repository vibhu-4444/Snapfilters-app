package com.snapfilters.feature.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapfilters.core.common.result.AppResult
import com.snapfilters.feature.chat.model.ChatMessage
import com.snapfilters.feature.chat.model.MessageKind
import com.snapfilters.feature.chat.repository.ChatRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface ChatUiEvent {
    data class ShowToast(val message: String) : ChatUiEvent
    data object ScrollToBottom : ChatUiEvent
}

data class ChatUiState(
    val conversationId: String,
    val conversationTitle: String,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false,
    val isAccessRevoked: Boolean = false,
    val unreadCount: Int = 0
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val conversationId: String,
    private val conversationTitle: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ChatUiState(conversationId = conversationId, conversationTitle = conversationTitle)
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ChatUiEvent>()
    val events: SharedFlow<ChatUiEvent> = _events.asSharedFlow()

    init {
        // Observe recent messages
        viewModelScope.launch {
            chatRepository.observeRecentMessages(conversationId).collect { msgList ->
                _uiState.update { it.copy(messages = msgList) }
            }
        }

        // Mark thread as read upon opening (Chapter 18)
        viewModelScope.launch {
            chatRepository.markConversationRead(conversationId)
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text.take(4000)) }
    }

    fun sendTextMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return

        val clientMsgId = UUID.randomUUID().toString()
        _uiState.update { it.copy(inputText = "", isSending = true) }

        viewModelScope.launch {
            val result = chatRepository.sendMessage(
                conversationId = conversationId,
                body = text,
                kind = MessageKind.TEXT,
                clientMessageId = clientMsgId
            )

            _uiState.update { it.copy(isSending = false) }

            when (result) {
                is AppResult.Success -> {
                    _events.emit(ChatUiEvent.ScrollToBottom)
                }
                is AppResult.Error -> {
                    _events.emit(ChatUiEvent.ShowToast(result.message))
                }
                AppResult.Loading -> Unit
            }
        }
    }

    fun sendPhotoMessage(storagePath: String) {
        val clientMsgId = UUID.randomUUID().toString()
        viewModelScope.launch {
            val result = chatRepository.sendMessage(
                conversationId = conversationId,
                body = null,
                kind = MessageKind.PHOTO,
                mediaPath = storagePath,
                clientMessageId = clientMsgId
            )
            if (result is AppResult.Success) {
                _events.emit(ChatUiEvent.ScrollToBottom)
            }
        }
    }

    fun retryMessage(clientMessageId: String) {
        val failedMsg = _uiState.value.messages.find { it.clientMessageId == clientMessageId } ?: return
        viewModelScope.launch {
            chatRepository.sendMessage(
                conversationId = conversationId,
                body = failedMsg.body,
                kind = failedMsg.kind,
                mediaPath = failedMsg.mediaStoragePath,
                clientMessageId = clientMessageId
            )
        }
    }
}
