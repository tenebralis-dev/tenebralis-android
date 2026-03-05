package com.tenebralis.dreamos.domain.usecase.chat

import com.tenebralis.dreamos.domain.model.Conversation
import com.tenebralis.dreamos.domain.model.ConversationMessage
import com.tenebralis.dreamos.domain.model.SessionState
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.ConversationRepository
import com.tenebralis.dreamos.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SendLocalMessageUseCaseTest {

    @Test
    fun `send success updates conversation last message`() = runBlocking {
        val authRepository = FakeAuthRepository(userId = "user-1")
        val conversationRepository = FakeConversationRepository()
        val updateLastMessageUseCase = UpdateConversationLastMessageUseCase(conversationRepository)
        val messageRepository = FakeMessageRepository()
        val useCase = SendLocalMessageUseCase(
            authRepository = authRepository,
            messageRepository = messageRepository,
            updateConversationLastMessageUseCase = updateLastMessageUseCase
        )

        val result = useCase(conversationId = "conversation-1", content = "  hello world  ")

        assertTrue(result.isSuccess)
        assertEquals(1, messageRepository.sendCallCount)
        assertEquals(1, conversationRepository.updateCallCount)
        assertEquals("conversation-1", conversationRepository.lastUpdatedConversationId)
        assertEquals("hello world", conversationRepository.lastUpdatedSummary)
    }

    @Test
    fun `send retries after first seq conflict and succeeds`() = runBlocking {
        val authRepository = FakeAuthRepository(userId = "user-1")
        val conversationRepository = FakeConversationRepository()
        val updateLastMessageUseCase = UpdateConversationLastMessageUseCase(conversationRepository)
        val messageRepository = FakeMessageRepository()
        messageRepository.sendBehavior = { message ->
            if (messageRepository.sendCallCount == 1) {
                Result.failure(
                    IllegalStateException(
                        "duplicate key value violates unique constraint messages_conversation_seq_unique"
                    )
                )
            } else {
                Result.success(message.copy(createdAt = "2026-02-16T00:00:00Z"))
            }
        }

        val useCase = SendLocalMessageUseCase(
            authRepository = authRepository,
            messageRepository = messageRepository,
            updateConversationLastMessageUseCase = updateLastMessageUseCase
        )

        val result = useCase(conversationId = "conversation-1", content = "retry test")

        assertTrue(result.isSuccess)
        assertEquals(2, messageRepository.sendCallCount)
        assertEquals(2, result.getOrThrow().seq)
        assertEquals(1, conversationRepository.updateCallCount)
    }

    @Test
    fun `send fails after more than three seq conflicts`() = runBlocking {
        val authRepository = FakeAuthRepository(userId = "user-1")
        val conversationRepository = FakeConversationRepository()
        val updateLastMessageUseCase = UpdateConversationLastMessageUseCase(conversationRepository)
        val messageRepository = FakeMessageRepository()
        messageRepository.sendBehavior = {
            Result.failure(
                IllegalStateException(
                    "duplicate key value violates unique constraint messages_conversation_seq_unique"
                )
            )
        }

        val useCase = SendLocalMessageUseCase(
            authRepository = authRepository,
            messageRepository = messageRepository,
            updateConversationLastMessageUseCase = updateLastMessageUseCase
        )

        val result = useCase(conversationId = "conversation-1", content = "always conflict")

        assertTrue(result.isFailure)
        assertEquals(4, messageRepository.sendCallCount)
        assertEquals(0, conversationRepository.updateCallCount)
        assertNotNull(result.exceptionOrNull())
    }
}

private class FakeAuthRepository(
    private val userId: String?
) : AuthRepository {

    override val sessionState: Flow<SessionState> = MutableStateFlow(
        if (userId == null) SessionState.NotAuthenticated else SessionState.Authenticated(userId)
    )

    override suspend fun signIn(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun signUp(email: String, password: String, username: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun verifyOtp(email: String, token: String): Result<Unit> = Result.success(Unit)
    override suspend fun resendOtp(email: String): Result<Unit> = Result.success(Unit)
    override suspend fun signOut(): Result<Unit> = Result.success(Unit)
    override fun getCurrentUserId(): String? = userId
}

private class FakeMessageRepository : MessageRepository {

    var sendCallCount: Int = 0
    var nextSeq: Int = 1
    var sendBehavior: (ConversationMessage) -> Result<ConversationMessage> = { message ->
        Result.success(message.copy(createdAt = "2026-02-16T00:00:00Z"))
    }

    override fun getByConversation(conversationId: String): Flow<Result<List<ConversationMessage>>> {
        return flowOf(Result.success(emptyList()))
    }

    override suspend fun send(message: ConversationMessage): Result<ConversationMessage> {
        sendCallCount += 1
        return sendBehavior(message)
    }

    override suspend fun getNextSeq(conversationId: String): Result<Int> {
        val current = nextSeq
        nextSeq += 1
        return Result.success(current)
    }
}

private class FakeConversationRepository : ConversationRepository {

    var updateCallCount: Int = 0
    var lastUpdatedConversationId: String? = null
    var lastUpdatedSummary: String? = null

    override fun getBySave(saveId: String): Flow<Result<List<Conversation>>> {
        return flowOf(Result.success(emptyList()))
    }

    override suspend fun getById(conversationId: String): Result<Conversation> {
        return Result.failure(UnsupportedOperationException("Not needed in this test"))
    }

    override suspend fun getOrCreate(
        saveId: String,
        npcId: String,
        threadKey: String,
        presetId: String?
    ): Result<Conversation> {
        return Result.failure(UnsupportedOperationException("Not needed in this test"))
    }

    override suspend fun updateLastMessage(
        conversationId: String,
        lastMessageAt: String,
        summary: String?
    ): Result<Unit> {
        updateCallCount += 1
        lastUpdatedConversationId = conversationId
        lastUpdatedSummary = summary
        return Result.success(Unit)
    }

    override suspend fun updateSettings(
        conversationId: String,
        presetId: String?,
        apiConnectionId: String?
    ): Result<Unit> {
        return Result.success(Unit)
    }
}
