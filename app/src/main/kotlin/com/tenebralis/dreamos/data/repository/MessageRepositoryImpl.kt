package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.mapper.toDto
import com.tenebralis.dreamos.data.remote.dto.ConversationMessageDto
import com.tenebralis.dreamos.domain.model.ConversationMessage
import com.tenebralis.dreamos.domain.repository.MessageRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class MessageRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : MessageRepository {

    override fun getByConversation(conversationId: String): Flow<Result<List<ConversationMessage>>> = flow {
        emit(
            runCatching {
                val userId = requireCurrentUserId()
                val normalizedConversationId = conversationId.trim()
                require(normalizedConversationId.isNotEmpty()) { "conversationId 不能为空" }

                fetchByConversation(userId = userId, conversationId = normalizedConversationId)
                    .map { it.toDomain() }
                    .sortedBy { it.seq }
            }
        )
    }.catch { emit(Result.failure(it)) }

    override suspend fun send(message: ConversationMessage): Result<ConversationMessage> = runCatching {
        val userId = requireCurrentUserId()
        validateForWrite(message = message, expectedUserId = userId)

        supabase.from(TABLE_CONVERSATION_MESSAGES)
            .insert(message.toDto()) {
                select()
            }
            .decodeSingle<ConversationMessageDto>()
            .toDomain()
    }

    override suspend fun getNextSeq(conversationId: String): Result<Int> = runCatching {
        val userId = requireCurrentUserId()
        val normalizedConversationId = conversationId.trim()
        require(normalizedConversationId.isNotEmpty()) { "conversationId 不能为空" }

        val maxSeq = fetchByConversation(userId = userId, conversationId = normalizedConversationId)
            .maxOfOrNull { it.seq }
            ?: 0

        maxSeq + 1
    }

    private suspend fun fetchByConversation(
        userId: String,
        conversationId: String
    ): List<ConversationMessageDto> {
        return supabase.from(TABLE_CONVERSATION_MESSAGES)
            .select {
                filter {
                    eq("user_id", userId)
                    eq("conversation_id", conversationId)
                }
            }
            .decodeList<ConversationMessageDto>()
    }

    private fun validateForWrite(message: ConversationMessage, expectedUserId: String) {
        require(message.id.isNotBlank()) { "message.id 不能为空" }
        require(message.userId == expectedUserId) { "message.userId 与当前会话不一致" }
        require(message.conversationId.trim().isNotEmpty()) { "message.conversationId 不能为空" }
        require(message.seq > 0) { "message.seq 必须大于 0" }
        require(message.content.trim().isNotEmpty()) { "message.content 不能为空" }
    }

    private fun requireCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")
    }

    private companion object {
        const val TABLE_CONVERSATION_MESSAGES = "conversation_messages"
    }
}
