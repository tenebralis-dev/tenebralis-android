package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.mapper.toDto
import com.tenebralis.dreamos.data.remote.dto.GlobalMemoryDto
import com.tenebralis.dreamos.domain.model.GlobalMemory
import com.tenebralis.dreamos.domain.repository.GlobalMemoryRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GlobalMemoryRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : GlobalMemoryRepository {

    override suspend fun getForContext(topN: Int): Result<List<GlobalMemory>> = runCatching {
        val userId = requireCurrentUserId()
        val now = Instant.now().toString()

        supabase.from(TABLE_GLOBAL_MEMORIES)
            .select {
                filter {
                    eq("user_id", userId)
                    exact("deleted_at", null)
                    eq("is_archived", false)
                    neq("ai_visibility", "private")
                }
            }
            .decodeList<GlobalMemoryDto>()
            .filter { dto ->
                // 过滤已过期的记忆
                dto.expiresAt == null || dto.expiresAt > now
            }
            .sortedWith(
                compareByDescending<GlobalMemoryDto> { it.isPinned }
                    .thenByDescending { it.importanceScore }
                    .thenByDescending { it.lastRecalledAt.orEmpty() }
            )
            .take(topN)
            .map { it.toDomain() }
    }

    override fun getAll(): Flow<Result<List<GlobalMemory>>> = flow {
        emit(
            runCatching {
                val userId = requireCurrentUserId()
                supabase.from(TABLE_GLOBAL_MEMORIES)
                    .select {
                        filter {
                            eq("user_id", userId)
                            exact("deleted_at", null)
                        }
                    }
                    .decodeList<GlobalMemoryDto>()
                    .map { it.toDomain() }
                    .sortedByDescending { it.updatedAt.orEmpty() }
            }
        )
    }.catch { emit(Result.failure(it)) }

    override suspend fun create(memory: GlobalMemory): Result<GlobalMemory> = runCatching {
        val userId = requireCurrentUserId()
        require(memory.userId == userId) { "memory.userId 与当前会话不一致" }

        supabase.from(TABLE_GLOBAL_MEMORIES)
            .insert(memory.toDto()) {
                select()
            }
            .decodeSingle<GlobalMemoryDto>()
            .toDomain()
    }

    override suspend fun update(memory: GlobalMemory): Result<GlobalMemory> = runCatching {
        val userId = requireCurrentUserId()
        require(memory.userId == userId) { "memory.userId 与当前会话不一致" }

        supabase.from(TABLE_GLOBAL_MEMORIES)
            .update(memory.toDto()) {
                filter {
                    eq("id", memory.id)
                    eq("user_id", userId)
                }
                select()
            }
            .decodeSingle<GlobalMemoryDto>()
            .toDomain()
    }

    override suspend fun softDelete(memoryId: String): Result<Unit> = runCatching {
        val userId = requireCurrentUserId()
        require(memoryId.isNotBlank()) { "memoryId 不能为空" }

        val existing = supabase.from(TABLE_GLOBAL_MEMORIES)
            .select {
                filter {
                    eq("id", memoryId)
                    eq("user_id", userId)
                }
            }
            .decodeSingle<GlobalMemoryDto>()

        supabase.from(TABLE_GLOBAL_MEMORIES)
            .update(existing.copy(deletedAt = Instant.now().toString())) {
                filter {
                    eq("id", memoryId)
                    eq("user_id", userId)
                }
            }
    }

    override suspend fun togglePin(memoryId: String, isPinned: Boolean): Result<Unit> = runCatching {
        val userId = requireCurrentUserId()
        require(memoryId.isNotBlank()) { "memoryId 不能为空" }

        val existing = supabase.from(TABLE_GLOBAL_MEMORIES)
            .select {
                filter {
                    eq("id", memoryId)
                    eq("user_id", userId)
                }
            }
            .decodeSingle<GlobalMemoryDto>()

        supabase.from(TABLE_GLOBAL_MEMORIES)
            .update(existing.copy(isPinned = isPinned)) {
                filter {
                    eq("id", memoryId)
                    eq("user_id", userId)
                }
            }
    }

    override suspend fun toggleArchive(memoryId: String, isArchived: Boolean): Result<Unit> = runCatching {
        val userId = requireCurrentUserId()
        require(memoryId.isNotBlank()) { "memoryId 不能为空" }

        val existing = supabase.from(TABLE_GLOBAL_MEMORIES)
            .select {
                filter {
                    eq("id", memoryId)
                    eq("user_id", userId)
                }
            }
            .decodeSingle<GlobalMemoryDto>()

        supabase.from(TABLE_GLOBAL_MEMORIES)
            .update(existing.copy(isArchived = isArchived)) {
                filter {
                    eq("id", memoryId)
                    eq("user_id", userId)
                }
            }
    }

    private fun requireCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")
    }

    private companion object {
        const val TABLE_GLOBAL_MEMORIES = "global_memories"
    }
}
