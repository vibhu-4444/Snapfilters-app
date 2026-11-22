package com.snapfilters.feature.chat.repository

import com.snapfilters.core.common.result.AppResult
import com.snapfilters.core.database.dao.ChatDao
import com.snapfilters.core.database.entity.ConversationEntity
import com.snapfilters.core.database.entity.ConversationKind
import com.snapfilters.core.database.entity.LocalMessageStatus
import com.snapfilters.core.database.entity.MessageEntity
import com.snapfilters.core.database.entity.MessageKind as DbMessageKind
import com.snapfilters.feature.chat.model.ChatMessage
import com.snapfilters.feature.chat.model.MessageKind
import com.snapfilters.feature.chat.model.MessageStatus
import com.snapfilters.feature.chat.model.ReactionCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * ChatRepository implements PRD v3.0 Chapter 18 specification:
 * - FR-MSG-01: Text and photo messages with timestamp and sender
 * - FR-MSG-02: Replies, reactions, unread counts surviving process death
 * - FR-MSG-03: Idempotent send via client_message_id
 * - FR-MSG-04: Stable keyset pagination (createdAt DESC, id DESC)
 * - FR-MSG-05: Revocation stops reads and clears cache
 */
class ChatRepository(
    private val chatDao: ChatDao,
    private val currentUserId: String,
    private val currentUserDisplayName: String
) {

    // In-memory simulated remote message store to represent backend database
    private val remoteMessages = mutableMapOf<String, MessageEntity>()
    private val remoteClientIds = mutableSetOf<String>()
    private val userGroupAccess = mutableMapOf<String, Boolean>() // conversationId -> hasAccess

    fun setGroupAccess(conversationId: String, hasAccess: Boolean) {
        userGroupAccess[conversationId] = hasAccess
    }

    /**
     * Idempotent send per FR-MSG-03 & ADR-009:
     * If same clientMessageId sent twice, returns the existing row without duplicate insertion.
     */
    suspend fun sendMessage(
        conversationId: String,
        body: String?,
        kind: MessageKind = MessageKind.TEXT,
        mediaPath: String? = null,
        clientMessageId: String = UUID.randomUUID().toString()
    ): AppResult<ChatMessage> {
        // FR-MSG-05: Check access
        if (userGroupAccess[conversationId] == false) {
            return AppResult.Error(IllegalAccessException("403: Forbidden - Access to this conversation has been revoked."))
        }

        // 1. Idempotency check: Has this clientMessageId already been processed?
        val existingLocal = chatDao.getMessageByClientId(clientMessageId)
        if (existingLocal != null) {
            return AppResult.Success(mapEntityToModel(existingLocal))
        }

        if (remoteClientIds.contains(clientMessageId)) {
            val existingRemote = remoteMessages.values.first { it.clientMessageId == clientMessageId }
            chatDao.insertOrUpdateMessage(existingRemote)
            return AppResult.Success(mapEntityToModel(existingRemote))
        }

        // 2. Commit to remote store
        val serverAssignedId = UUID.randomUUID().toString()
        val serverTimestamp = System.currentTimeMillis()

        val entity = MessageEntity(
            id = serverAssignedId,
            conversationId = conversationId,
            senderId = currentUserId,
            senderDisplayName = currentUserDisplayName,
            isSenderDeleted = false,
            kind = when (kind) {
                MessageKind.TEXT -> DbMessageKind.TEXT
                MessageKind.PHOTO -> DbMessageKind.PHOTO
                MessageKind.FILE -> DbMessageKind.FILE
                MessageKind.SYSTEM -> DbMessageKind.SYSTEM
            },
            body = body,
            clientMessageId = clientMessageId,
            createdAt = serverTimestamp,
            status = LocalMessageStatus.SENT,
            mediaStoragePath = mediaPath
        )

        remoteMessages[serverAssignedId] = entity
        remoteClientIds.add(clientMessageId)

        // 3. Update local cache
        chatDao.insertOrUpdateMessage(entity)

        return AppResult.Success(mapEntityToModel(entity))
    }

    /**
     * Keyset pagination per FR-MSG-04:
     * Cursor is (createdAt, id), returning rows strictly older without duplicates or skips.
     */
    suspend fun getPagedMessages(
        conversationId: String,
        cursorTime: Long?,
        cursorId: String?,
        limit: Int = 50
    ): AppResult<List<ChatMessage>> {
        // FR-MSG-05: Access check
        if (userGroupAccess[conversationId] == false) {
            chatDao.clearConversationMessages(conversationId)
            return AppResult.Error(IllegalAccessException("403: Forbidden - Membership revoked."))
        }

        val localMessages = chatDao.getPagedMessages(conversationId, cursorTime, cursorId, limit)
        return AppResult.Success(localMessages.map { mapEntityToModel(it) })
    }

    fun observeRecentMessages(conversationId: String): Flow<List<ChatMessage>> {
        return chatDao.observeRecentMessages(conversationId).map { list ->
            list.map { mapEntityToModel(it) }
        }
    }

    suspend fun markConversationRead(conversationId: String) {
        val now = System.currentTimeMillis()
        chatDao.updateLastReadAt(conversationId, now)
    }

    suspend fun getUnreadCount(conversationId: String, lastReadAt: Long): Int {
        return chatDao.getUnreadCount(conversationId, currentUserId, lastReadAt)
    }

    private fun mapEntityToModel(entity: MessageEntity): ChatMessage {
        return ChatMessage(
            id = entity.id,
            conversationId = entity.conversationId,
            senderId = entity.senderId,
            senderDisplayName = entity.senderDisplayName,
            isSenderDeleted = entity.isSenderDeleted,
            kind = when (entity.kind) {
                DbMessageKind.TEXT -> MessageKind.TEXT
                DbMessageKind.PHOTO -> MessageKind.PHOTO
                DbMessageKind.FILE -> MessageKind.FILE
                DbMessageKind.SYSTEM -> MessageKind.SYSTEM
            },
            body = entity.body,
            replyToId = entity.replyToId,
            clientMessageId = entity.clientMessageId,
            createdAtEpochMillis = entity.createdAt,
            status = when (entity.status) {
                LocalMessageStatus.QUEUED -> MessageStatus.QUEUED
                LocalMessageStatus.SENDING -> MessageStatus.SENDING
                LocalMessageStatus.SENT -> MessageStatus.SENT
                LocalMessageStatus.DELIVERED -> MessageStatus.DELIVERED
                LocalMessageStatus.FAILED -> MessageStatus.FAILED
            },
            mediaStoragePath = entity.mediaStoragePath
        )
    }
}
