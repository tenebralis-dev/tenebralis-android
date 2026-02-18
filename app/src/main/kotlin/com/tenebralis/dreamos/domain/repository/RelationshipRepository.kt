package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.Relationship
import kotlinx.coroutines.flow.Flow

/**
 * 好感度仓库接口
 *
 * 对应表：user_npc_relationships
 */
interface RelationshipRepository {

    /** 获取指定世界的所有 NPC 关系 */
    fun getRelationships(worldId: String): Flow<Result<List<Relationship>>>

    /** 获取与特定 NPC 的关系 */
    suspend fun getRelationship(worldId: String, npcId: String): Result<Relationship?>

    /** 调整好感度（delta 可正可负） */
    suspend fun adjustAffinity(relationshipId: String, delta: Int): Result<Relationship>

    /** 获取或创建关系（如首次互动则创建默认中立关系） */
    suspend fun getOrCreateRelationship(worldId: String, npcId: String): Result<Relationship>
}
