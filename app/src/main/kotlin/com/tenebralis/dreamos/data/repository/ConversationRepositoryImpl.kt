package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.remote.dto.ConversationDto
import com.tenebralis.dreamos.domain.model.Conversation
import com.tenebralis.dreamos.domain.repository.ConversationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class ConversationRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : ConversationRepository {

    override fun getBySave(saveId: String): Flow<Result<List<Conversation>>> = flow {
        emit(
            runCatching {
                val userId = requireCurrentUserId()
                val normalizedSaveId = saveId.trim()
                require(normalizedSaveId.isNotEmpty()) { "saveId 不能为空" }

                fetchBySave(userId = userId, saveId = normalizedSaveId)
                    .map { it.toDomain() }
                    .sortedByDescending { it.lastMessageAt.orEmpty() }
            }
        )
    }.catch { emit(Result.failure(it)) }

    override suspend fun getById(conversationId: String): Result<Conversation> = runCatching {
        val userId = requireCurrentUserId()
        val normalizedId = conversationId.trim()
        require(normalizedId.isNotEmpty()) { "conversationId 不能为空" }
        supabase.from(TABLE_CONVERSATIONS)
            .select {
                filter {
                    eq("id", normalizedId)
                    eq("user_id", userId)
                }
            }
            .decodeSingle<ConversationDto>()
            .toDomain()
    }

    override suspend fun getOrCreate(
        saveId: String,
        npcId: String,
        threadKey: String,
        presetId: String?
    ): Result<Conversation> = runCatching {
        val userId = requireCurrentUserId()
        val normalizedSaveId = saveId.trim()
        val normalizedNpcId = npcId.trim()
        val normalizedThreadKey = threadKey.trim()

        require(normalizedSaveId.isNotEmpty()) { "saveId 不能为空" }
        require(normalizedNpcId.isNotEmpty()) { "npcId 不能为空" }
        require(normalizedThreadKey.isNotEmpty()) { "threadKey 不能为空" }

        fetchByThread(
            userId = userId,
            saveId = normalizedSaveId,
            npcId = normalizedNpcId,
            threadKey = normalizedThreadKey
        )?.toDomain()?.let { existing ->
            return@runCatching existing
        }

        val draft = ConversationDto(
            id = UUID.randomUUID().toString(),
            userId = userId,
            saveId = normalizedSaveId,
            npcId = normalizedNpcId,
            threadKey = normalizedThreadKey,
            title = null,
            summary = null,
            pinnedContextText = null,
            presetId = presetId,
            apiConnectionId = null,
            lastMessageAt = null,
            createdAt = null,
            updatedAt = null
        )

        runCatching {
            supabase.from(TABLE_CONVERSATIONS)
                .insert(draft) {
                    select()
                }
                .decodeSingle<ConversationDto>()
        }.recoverCatching { error ->
            if (isThreadUniqueConflict(error)) {
                fetchByThread(
                    userId = userId,
                    saveId = normalizedSaveId,
                    npcId = normalizedNpcId,
                    threadKey = normalizedThreadKey
                ) ?: throw error
            } else {
                throw error
            }
        }.getOrThrow().toDomain()
    }

    override suspend fun updateLastMessage(
        conversationId: String,
        lastMessageAt: String,
        summary: String?
    ): Result<Unit> = runCatching {
        val userId = requireCurrentUserId()
        val normalizedConversationId = conversationId.trim()
        val normalizedLastMessageAt = lastMessageAt.trim()

        require(normalizedConversationId.isNotEmpty()) { "conversationId 不能为空" }
        require(normalizedLastMessageAt.isNotEmpty()) { "lastMessageAt 不能为空" }

        val existing = supabase.from(TABLE_CONVERSATIONS)
            .select {
                filter {
                    eq("id", normalizedConversationId)
                    eq("user_id", userId)
                }
            }
            .decodeSingle<ConversationDto>()

        supabase.from(TABLE_CONVERSATIONS)
            .update(
                existing.copy(
                    lastMessageAt = normalizedLastMessageAt,
                    summary = summary?.trim()?.takeIf { it.isNotEmpty() }
                )
            ) {
                filter {
                    eq("id", normalizedConversationId)
                    eq("user_id", userId)
                }
            }
    }

    override suspend fun updateSettings(
        conversationId: String,
        presetId: String?,
        apiConnectionId: String?
    ): Result<Unit> = runCatching {
        val userId = requireCurrentUserId()
        val normalizedId = conversationId.trim()
        require(normalizedId.isNotEmpty()) { "conversationId 不能为空" }

        val existing = supabase.from(TABLE_CONVERSATIONS)
            .select {
                filter {
                    eq("id", normalizedId)
                    eq("user_id", userId)
                }
            }
            .decodeSingle<ConversationDto>()

        supabase.from(TABLE_CONVERSATIONS)
            .update(
                existing.copy(
                    presetId = presetId,
                    apiConnectionId = apiConnectionId
                )
            ) {
                filter {
                    eq("id", normalizedId)
                    eq("user_id", userId)
                }
            }
    }

    private suspend fun fetchBySave(userId: String, saveId: String): List<ConversationDto> {
        return supabase.from(TABLE_CONVERSATIONS)
            .select {
                filter {
                    eq("user_id", userId)
                    eq("save_id", saveId)
                }
            }
            .decodeList<ConversationDto>()
    }

    private suspend fun fetchByThread(
        userId: String,
        saveId: String,
        npcId: String,
        threadKey: String
    ): ConversationDto? {
        return supabase.from(TABLE_CONVERSATIONS)
            .select {
                filter {
                    eq("user_id", userId)
                    eq("save_id", saveId)
                    eq("npc_id", npcId)
                    eq("thread_key", threadKey)
                }
            }
            .decodeList<ConversationDto>()
            .firstOrNull()
    }

    private fun isThreadUniqueConflict(error: Throwable): Boolean {
        val lowered = error.message.orEmpty().lowercase()
        return "conversations_thread_unique" in lowered || "duplicate key" in lowered
    }

    private fun requireCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")
    }

    private companion object {
        const val TABLE_CONVERSATIONS = "conversations"
    }
}
