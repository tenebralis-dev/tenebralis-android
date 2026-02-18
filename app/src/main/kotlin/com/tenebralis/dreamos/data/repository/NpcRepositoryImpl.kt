package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.mapper.toDto
import com.tenebralis.dreamos.data.remote.dto.NpcDto
import com.tenebralis.dreamos.domain.model.Npc
import com.tenebralis.dreamos.domain.repository.NpcRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class NpcRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : NpcRepository {

    override fun getByUser(): Flow<Result<List<Npc>>> = flow {
        emit(
            runCatching {
                val userId = requireCurrentUserId()
                fetchByUser(userId)
                    .map { it.toDomain() }
                    .sortedBy { it.name.lowercase() }
            }
        )
    }.catch { emit(Result.failure(it)) }

    override suspend fun getById(npcId: String): Result<Npc> = runCatching {
        val userId = requireCurrentUserId()
        require(npcId.isNotBlank()) { "npcId 不能为空" }
        supabase.from(TABLE_NPCS)
            .select {
                filter {
                    eq("id", npcId)
                    eq("user_id", userId)
                }
            }
            .decodeSingle<NpcDto>()
            .toDomain()
    }

    override suspend fun create(npc: Npc): Result<Npc> = runCatching {
        val userId = requireCurrentUserId()
        validateForWrite(npc = npc, expectedUserId = userId)

        supabase.from(TABLE_NPCS)
            .insert(npc.toDto()) {
                select()
            }
            .decodeSingle<NpcDto>()
            .toDomain()
    }

    override suspend fun update(npc: Npc): Result<Npc> = runCatching {
        val userId = requireCurrentUserId()
        validateForWrite(npc = npc, expectedUserId = userId)

        supabase.from(TABLE_NPCS)
            .update(npc.toDto()) {
                filter {
                    eq("id", npc.id)
                    eq("user_id", userId)
                }
                select()
            }
            .decodeSingle<NpcDto>()
            .toDomain()
    }

    private suspend fun fetchByUser(userId: String): List<NpcDto> {
        return supabase.from(TABLE_NPCS)
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<NpcDto>()
    }

    private fun validateForWrite(npc: Npc, expectedUserId: String) {
        require(npc.id.isNotBlank()) { "npc.id 不能为空" }
        require(npc.userId == expectedUserId) { "npc.userId 与当前会话不一致" }
        require(npc.name.trim().isNotEmpty()) { "npc.name 不能为空" }
    }

    private fun requireCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")
    }

    private companion object {
        const val TABLE_NPCS = "npcs"
    }
}
