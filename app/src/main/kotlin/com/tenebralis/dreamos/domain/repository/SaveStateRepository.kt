package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.WorldSaveState
import kotlinx.coroutines.flow.Flow

/**
 * 存档状态仓库接口
 *
 * 对应表：world_save_states
 */
interface SaveStateRepository {

    /** 获取指定身份下的所有存档 */
    fun getByIdentity(identityId: String): Flow<Result<List<WorldSaveState>>>

    /** 根据 ID 获取存档详情 */
    suspend fun getById(saveId: String): Result<WorldSaveState>

    /** 创建新存档 */
    suspend fun create(saveState: WorldSaveState): Result<WorldSaveState>

    /** 更新存档 */
    suspend fun update(saveState: WorldSaveState): Result<WorldSaveState>
}
