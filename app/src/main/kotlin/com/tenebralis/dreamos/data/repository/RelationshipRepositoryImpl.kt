package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.mapper.toDto
import com.tenebralis.dreamos.data.remote.dto.NpcDto
import com.tenebralis.dreamos.data.remote.dto.RelationshipDto
import com.tenebralis.dreamos.domain.model.AffinityTiers
import com.tenebralis.dreamos.domain.model.Relationship
import com.tenebralis.dreamos.domain.repository.RelationshipRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class RelationshipRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : RelationshipRepository {

    override fun getRelationships(worldId: String): Flow<Result<List<Relationship>>> = flow {
        emit(runCatching {
            val userId = requireCurrentUserId()

            val relDtos = supabase.from(TABLE_RELATIONSHIPS)
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("world_id", worldId)
                    }
                    order("affinity", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<RelationshipDto>()

            if (relDtos.isEmpty()) return@runCatching emptyList()

            // 批量查询 NPC 名称
            val npcIds = relDtos.map { it.npcId }.distinct()
            val npcDtos = supabase.from(TABLE_NPCS)
                .select {
                    filter {
                        eq("user_id", userId)
                        isIn("id", npcIds)
                    }
                }
                .decodeList<NpcDto>()
            val npcNameMap = npcDtos.associate { it.id to it.name }

            relDtos.map { dto ->
                dto.toDomain(npcName = npcNameMap[dto.npcId]).let { rel ->
                    // 客户端重新计算 status
                    rel.copy(status = AffinityTiers.getTier(rel.affinity).key)
                }
            }
        })
    }.catch { emit(Result.failure(it)) }

    override suspend fun getRelationship(
        worldId: String,
        npcId: String
    ): Result<Relationship?> = runCatching {
        val userId = requireCurrentUserId()
        supabase.from(TABLE_RELATIONSHIPS)
            .select {
                filter {
                    eq("user_id", userId)
                    eq("world_id", worldId)
                    eq("npc_id", npcId)
                }
            }
            .decodeList<RelationshipDto>()
            .firstOrNull()
            ?.toDomain()
    }

    override suspend fun adjustAffinity(
        relationshipId: String,
        delta: Int
    ): Result<Relationship> = runCatching {
        val userId = requireCurrentUserId()

        val current = supabase.from(TABLE_RELATIONSHIPS)
            .select {
                filter {
                    eq("id", relationshipId)
                    eq("user_id", userId)
                }
            }
            .decodeSingle<RelationshipDto>()

        val newAffinity = (current.affinity + delta).coerceIn(-100, 100)
        val newStatus = AffinityTiers.getTier(newAffinity).key

        supabase.from(TABLE_RELATIONSHIPS)
            .update({
                set("affinity", newAffinity)
                set("status", newStatus)
            }) {
                filter {
                    eq("id", relationshipId)
                    eq("user_id", userId)
                }
                select()
            }
            .decodeSingle<RelationshipDto>()
            .toDomain()
    }

    override suspend fun getOrCreateRelationship(
        worldId: String,
        npcId: String
    ): Result<Relationship> = runCatching {
        val userId = requireCurrentUserId()

        val existing = supabase.from(TABLE_RELATIONSHIPS)
            .select {
                filter {
                    eq("user_id", userId)
                    eq("world_id", worldId)
                    eq("npc_id", npcId)
                }
            }
            .decodeList<RelationshipDto>()
            .firstOrNull()

        if (existing != null) return@runCatching existing.toDomain()

        val newRel = Relationship(
            id = UUID.randomUUID().toString(),
            userId = userId,
            worldId = worldId,
            npcId = npcId,
            affinity = 0,
            status = "neutral"
        )
        supabase.from(TABLE_RELATIONSHIPS)
            .insert(newRel.toDto()) { select() }
            .decodeSingle<RelationshipDto>()
            .toDomain()
    }

    private fun requireCurrentUserId(): String =
        supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")

    private companion object {
        const val TABLE_RELATIONSHIPS = "user_npc_relationships"
        const val TABLE_NPCS = "npcs"
    }
}
