package com.snapfilters.feature.chat

import com.snapfilters.core.common.result.AppResult
import com.snapfilters.core.database.dao.ChatDao
import com.snapfilters.core.database.entity.MessageEntity
import com.snapfilters.feature.chat.model.ChatMessage
import com.snapfilters.feature.chat.model.MessageKind
import com.snapfilters.feature.chat.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class ChatRepositoryTest {

    private lateinit var mockChatDao: ChatDao
    private lateinit var chatRepository: ChatRepository
    private val currentUserId = "user-alice-123"
    private val currentUserDisplayName = "Alice"
    private val testConversationId = "conv-group-456"

    @Before
    fun setUp() {
        mockChatDao = mockk(relaxed = true)
        chatRepository = ChatRepository(mockChatDao, currentUserId, currentUserDisplayName)
        chatRepository.setGroupAccess(testConversationId, true)
    }

    // FR-MSG-03: Idempotent send; retry does not duplicate
    @Test
    fun fr_msg_03_same_client_message_id_sent_twice_yields_one_row() = runTest {
        val clientMessageId = "unique-client-id-789"
        coEvery { mockChatDao.getMessageByClientId(clientMessageId) } returns null

        // First send
        val firstResult = chatRepository.sendMessage(
            conversationId = testConversationId,
            body = "Golden hour capture",
            clientMessageId = clientMessageId
        )
        assertTrue(firstResult is AppResult.Success)
        val firstMsg = (firstResult as AppResult.Success).data
        assertEquals("Golden hour capture", firstMsg.body)
        assertEquals(clientMessageId, firstMsg.clientMessageId)

        // Second send with same clientMessageId (e.g. network retry)
        val secondResult = chatRepository.sendMessage(
            conversationId = testConversationId,
            body = "Golden hour capture",
            clientMessageId = clientMessageId
        )
        assertTrue(secondResult is AppResult.Success)
        val secondMsg = (secondResult as AppResult.Success).data

        // Must yield the exact same message ID
        assertEquals(firstMsg.id, secondMsg.id)
    }

    // FR-MSG-04: Pagination of history, stable order without skips or duplicates
    @Test
    fun fr_msg_04_keyset_pagination_returns_stable_rows() = runTest {
        val dummyEntities = (1..50).map { i ->
            MessageEntity(
                id = "msg-$i",
                conversationId = testConversationId,
                senderId = "sender-$i",
                senderDisplayName = "User $i",
                body = "Message body $i",
                clientMessageId = "client-$i",
                createdAt = 100000L - i * 100L
            )
        }

        coEvery { mockChatDao.getPagedMessages(testConversationId, any(), any(), any()) } returns dummyEntities

        val result = chatRepository.getPagedMessages(testConversationId, null, null, 50)
        assertTrue(result is AppResult.Success)
        val paged = (result as AppResult.Success).data
        assertEquals(50, paged.size)

        // Verify sorted by createdAt descending
        for (i in 0 until paged.size - 1) {
            assertTrue(paged[i].createdAtEpochMillis >= paged[i + 1].createdAtEpochMillis)
        }
    }

    // FR-MSG-02: Unread count survives process death and is derived from last_read_at
    @Test
    fun fr_msg_02_unread_count_calculated_from_last_read_at_timestamp() = runTest {
        val lastReadAt = 50000L
        coEvery { mockChatDao.getUnreadCount(testConversationId, currentUserId, lastReadAt) } returns 4

        val unreadCount = chatRepository.getUnreadCount(testConversationId, lastReadAt)
        assertEquals(4, unreadCount)

        // Opening conversation marks it read
        chatRepository.markConversationRead(testConversationId)
        coVerify { mockChatDao.updateLastReadAt(testConversationId, any()) }
    }

    // FR-MSG-05: Revoked membership stops reads immediately and clears cache
    @Test
    fun fr_msg_05_revoked_membership_stops_reads_and_clears_cache() = runTest {
        chatRepository.setGroupAccess(testConversationId, false)

        val result = chatRepository.getPagedMessages(testConversationId, null, null, 50)
        assertTrue(result is AppResult.Error)
        assertEquals("403: Forbidden - Membership revoked.", (result as AppResult.Error).message)

        coVerify { mockChatDao.clearConversationMessages(testConversationId) }
    }

    // Tombstone sender resolution: author shown as "Former member"
    @Test
    fun tombstoned_author_displays_as_former_member() {
        val tombstoneMessage = ChatMessage(
            id = "msg-999",
            conversationId = testConversationId,
            senderId = "user-departed",
            senderDisplayName = "Departed User",
            isSenderDeleted = true,
            body = "Historic photograph comment",
            clientMessageId = "client-999",
            createdAtEpochMillis = System.currentTimeMillis()
        )

        assertEquals("Former member", tombstoneMessage.effectiveSenderName)
    }
}
