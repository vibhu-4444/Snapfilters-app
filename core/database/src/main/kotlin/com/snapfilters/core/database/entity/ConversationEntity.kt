package com.snapfilters.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ConversationKind {
    DIRECT,
    GROUP
}

@Entity(tableName = "cached_conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val kind: ConversationKind = ConversationKind.GROUP,
    val groupId: String? = null,
    val title: String,
    val lastMessagePreview: String?,
    val lastMessageAt: Long = System.currentTimeMillis(),
    val lastReadAt: Long = 0L // Epoch timestamp for unread calculation (PRD Chapter 18)
)
