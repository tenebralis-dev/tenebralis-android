package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.WorldNpcPersona
import kotlinx.coroutines.flow.Flow

/**
 * NPC 世界人格仓库接口
 *
 * 对应表：world_npc_personas
 */
interface WorldNpcPersonaRepository {

    /** 获取世界内所有 NPC 人格 */
    fun getByWorld(worldId: String): Flow<Result<List<WorldNpcPersona>>>

    /** 根据 ID 获取 NPC 人格 */
    suspend fun getById(personaId: String): Result<WorldNpcPersona>

    /** 创建 NPC 人格 */
    suspend fun create(
        worldId: String,
        npcId: String,
        displayName: String,
        personaJson: String = "{}",
        avatarUrl: String? = null,
        promptText: String? = null
    ): Result<WorldNpcPersona>
}
