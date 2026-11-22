package com.snapfilters.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class LocalMessageStatus {
    QUEUED,
    SENDING,
    SENT,
    DELIVERED,
    FAILED
}

enum class MessageKind {
    TEXT,
    PHOTO,
    FILE,
    SYSTEM
}

@Entity(
    tableName = "cached_messages",
    indices = [
        Index(value = ["conversationId", "createdAt", "id"]),
        Index(value = ["clientMessageId"], unique = true)
    ]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val senderDisplayName: String,
    val isSenderDeleted: Boolean = false,
    val kind: MessageKind = MessageKind.TEXT,
    val body: String?,
    val replyToId: String? = null,
    val clientMessageId: String, // ADR-009 & FR-MSG-03: Device idempotency key
    val createdAt: Long = System.currentTimeMillis(),
    val status: LocalMessageStatus = LocalMessageStatus.SENT,
    val mediaStoragePath: String? = null,
    val mediaWidth: Int? = null,
    val mediaHeight: Int? = null,
    val reactionsSummary: String? = null // Serialized reactions JSON
)
