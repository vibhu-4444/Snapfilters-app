package com.snapfilters.feature.chat.model

enum class MessageStatus {
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

enum class ReactionCode(val emoji: String) {
    HEART("❤️"),
    LAUGH("😂"),
    WOW("😮"),
    SAD("😢"),
    LIKE("👍")
}

data class MessageReaction(
    val code: ReactionCode,
    val count: Int,
    val userReacted: Boolean = false
)

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderDisplayName: String,
    val isSenderDeleted: Boolean = false,
    val kind: MessageKind = MessageKind.TEXT,
    val body: String?,
    val replyToId: String? = null,
    val clientMessageId: String, // Idempotency key (FR-MSG-03)
    val createdAtEpochMillis: Long,
    val status: MessageStatus = MessageStatus.SENT,
    val mediaStoragePath: String? = null,
    val reactions: List<MessageReaction> = emptyList(),
    val readByCount: Int = 0 // In groups: "read by X of Y" (Chapter 18)
) {
    val effectiveSenderName: String
        get() = if (isSenderDeleted) "Former member" else senderDisplayName
}
