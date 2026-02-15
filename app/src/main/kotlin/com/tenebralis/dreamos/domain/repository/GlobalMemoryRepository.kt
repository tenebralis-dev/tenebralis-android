package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.GlobalMemory
import kotlinx.coroutines.flow.Flow

/**
 * 全局记忆仓库接口
 *
 * 对应表：global_memories
 */
interface GlobalMemoryRepository {

    /**
     * 获取用于聊天上下文的记忆列表
     *
     * 仅返回 deleted_at is null、is_archived = false、未过期、
     * ai_visibility 合规的记忆，按 is_pinned DESC + importance_score DESC + 最近召回时间排序。
     *
     * @param topN 返回数量上限
     */
    suspend fun getForContext(topN: Int = 30): Result<List<GlobalMemory>>

    /** 获取当前用户的所有记忆（含归档，不含软删除） */
    fun getAll(): Flow<Result<List<GlobalMemory>>>

    /** 创建新记忆 */
    suspend fun create(memory: GlobalMemory): Result<GlobalMemory>

    /** 更新记忆 */
    suspend fun update(memory: GlobalMemory): Result<GlobalMemory>

    /** 软删除记忆（设置 deleted_at） */
    suspend fun softDelete(memoryId: String): Result<Unit>

    /** 切换置顶状态 */
    suspend fun togglePin(memoryId: String, isPinned: Boolean): Result<Unit>

    /** 切换归档状态 */
    suspend fun toggleArchive(memoryId: String, isArchived: Boolean): Result<Unit>
}
