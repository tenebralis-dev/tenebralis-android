package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.World
import kotlinx.coroutines.flow.Flow

/**
 * 世界仓库接口
 *
 * 对应表：worlds
 */
interface WorldRepository {

    /** 获取当前用户的所有世界 */
    fun getWorlds(): Flow<Result<List<World>>>

    /** 根据 ID 获取世界详情 */
    suspend fun getById(worldId: String): Result<World>

    /** 创建新世界 */
    suspend fun create(world: World): Result<World>

    /** 更新世界 */
    suspend fun update(world: World): Result<World>

    /** 删除世界（软删除：设置 status = deleted） */
    suspend fun delete(worldId: String): Result<Unit>

    /** 根据名称查找世界（用于系统世界等固定名称查找） */
    suspend fun getByName(name: String): Result<World?>
}
