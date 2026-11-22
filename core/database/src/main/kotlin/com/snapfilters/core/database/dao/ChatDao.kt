package com.snapfilters.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.snapfilters.core.database.entity.ConversationEntity
import com.snapfilters.core.database.entity.LocalMessageStatus
import com.snapfilters.core.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    // Idempotent insertion: If same clientMessageId exists, ignore or replace
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMessage(message: MessageEntity)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("SELECT * FROM cached_messages WHERE id = :id")
    suspend fun getMessageById(id: String): MessageEntity?

    @Query("SELECT * FROM cached_messages WHERE clientMessageId = :clientMessageId")
    suspend fun getMessageByClientId(clientMessageId: String): MessageEntity?

    /**
     * Keyset pagination query per PRD v3.0 Chapter 18:
     * Order by (createdAt DESC, id DESC), keyset cursor (cursorTime, cursorId)
     */
    @Query("""
        SELECT * FROM cached_messages 
        WHERE conversationId = :conversationId 
        AND (:cursorTime IS NULL OR (createdAt < :cursorTime OR (createdAt = :cursorTime AND id < :cursorId)))
        ORDER BY createdAt DESC, id DESC 
        LIMIT :limit
    """)
    suspend fun getPagedMessages(
        conversationId: String,
        cursorTime: Long?,
        cursorId: String?,
        limit: Int = 50
    ): List<MessageEntity>

    @Query("SELECT * FROM cached_messages WHERE conversationId = :conversationId ORDER BY createdAt DESC, id DESC LIMIT 50")
    fun observeRecentMessages(conversationId: String): Flow<List<MessageEntity>>

    // Unread count calculated from conversation.lastReadAt (survives app restart per FR-MSG-02)
    @Query("""
        SELECT COUNT(*) FROM cached_messages 
        WHERE conversationId = :conversationId 
        AND senderId != :currentUserId
        AND createdAt > :lastReadAt
    """)
    suspend fun getUnreadCount(conversationId: String, currentUserId: String, lastReadAt: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conv: ConversationEntity)

    @Query("SELECT * FROM cached_conversations ORDER BY lastMessageAt DESC")
    fun observeConversations(): Flow<List<ConversationEntity>>

    @Query("UPDATE cached_conversations SET lastReadAt = :readAt WHERE id = :convId")
    suspend fun updateLastReadAt(convId: String, readAt: Long)

    @Query("DELETE FROM cached_messages WHERE conversationId = :convId")
    suspend fun clearConversationMessages(convId: String)
}
