package com.tenebralis.dreamos.domain.usecase.chat

import com.tenebralis.dreamos.domain.model.Conversation
import com.tenebralis.dreamos.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetOrCreateConversationUseCaseTest {

    @Test
    fun `returns existing conversation when repository finds it`() = runBlocking {
        val existing = buildConversation(id = "conversation-existing")
        val repository = StubConversationRepository { _, _, _ ->
            Result.success(existing)
        }
        val useCase = GetOrCreateConversationUseCase(repository)

        val result = useCase(
            saveId = "save-1",
            npcId = "npc-1",
            threadKey = GetOrCreateConversationUseCase.DEFAULT_THREAD_KEY
        )

        assertTrue(result.isSuccess)
        assertEquals("conversation-existing", result.getOrThrow().id)
    }

    @Test
    fun `creates conversation when repository returns newly created`() = runBlocking {
        val created = buildConversation(id = "conversation-created")
        val repository = StubConversationRepository { _, _, _ ->
            Result.success(created)
        }
        val useCase = GetOrCreateConversationUseCase(repository)

        val result = useCase(
            saveId = "save-1",
            npcId = "npc-2",
            threadKey = "main"
        )

        assertTrue(result.isSuccess)
        assertEquals("conversation-created", result.getOrThrow().id)
    }

    @Test
    fun `returns success when repository resolves unique conflict by fallback query`() = runBlocking {
        val conflictResolved = buildConversation(id = "conversation-conflict-resolved")
        val repository = StubConversationRepository { _, _, _ ->
            // 模拟 Repository 内部已处理“唯一冲突后回查”并返回成功。
            Result.success(conflictResolved)
        }
        val useCase = GetOrCreateConversationUseCase(repository)

        val result = useCase(
            saveId = "save-2",
            npcId = "npc-9",
            threadKey = "main"
        )

        assertTrue(result.isSuccess)
        assertEquals("conversation-conflict-resolved", result.getOrThrow().id)
    }
}

private class StubConversationRepository(
    private val onGetOrCreate: suspend (saveId: String, npcId: String, threadKey: String) -> Result<Conversation>
) : ConversationRepository {

    override fun getBySave(saveId: String): Flow<Result<List<Conversation>>> {
        return flowOf(Result.success(emptyList()))
    }

    override suspend fun getById(conversationId: String): Result<Conversation> {
        return Result.failure(NotImplementedError("stub"))
    }

    override suspend fun getOrCreate(
        saveId: String,
        npcId: String,
        threadKey: String,
        presetId: String?
    ): Result<Conversation> {
        return onGetOrCreate(saveId, npcId, threadKey)
    }

    override suspend fun updateLastMessage(
        conversationId: String,
        lastMessageAt: String,
        summary: String?
    ): Result<Unit> {
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

private fun buildConversation(id: String): Conversation {
    return Conversation(
        id = id,
        userId = "user-1",
        saveId = "save-1",
        npcId = "npc-1",
        threadKey = "main",
        title = null,
        summary = null,
        pinnedContextText = null,
        presetId = null,
        apiConnectionId = null,
        lastMessageAt = null,
        createdAt = null,
        updatedAt = null
    )
}
