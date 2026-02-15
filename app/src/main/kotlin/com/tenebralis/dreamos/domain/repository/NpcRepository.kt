package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.Npc
import kotlinx.coroutines.flow.Flow

/**
 * NPC 仓库接口
 *
 * 对应表：npcs
 */
interface NpcRepository {

    /** 获取当前用户的所有 NPC */
    fun getByUser(): Flow<Result<List<Npc>>>

    /** 创建新 NPC */
    suspend fun create(npc: Npc): Result<Npc>

    /** 更新 NPC */
    suspend fun update(npc: Npc): Result<Npc>
}
