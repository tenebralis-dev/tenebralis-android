package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.UserWorldIdentity
import kotlinx.coroutines.flow.Flow

/**
 * 世界身份仓库接口
 *
 * 对应表：user_world_identities
 */
interface IdentityRepository {

    /** 获取指定世界下的所有身份 */
    fun getByWorld(worldId: String): Flow<Result<List<UserWorldIdentity>>>

    /** 创建新身份 */
    suspend fun create(identity: UserWorldIdentity): Result<UserWorldIdentity>

    /** 更新身份 */
    suspend fun update(identity: UserWorldIdentity): Result<UserWorldIdentity>

    /** 设置指定身份为活跃（同时将该世界下其他身份设为非活跃） */
    suspend fun setActive(worldId: String, identityId: String): Result<Unit>
}
