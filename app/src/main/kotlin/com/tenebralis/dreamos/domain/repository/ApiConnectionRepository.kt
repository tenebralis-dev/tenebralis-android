package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.ApiConnection
import kotlinx.coroutines.flow.Flow

/**
 * API 连接仓库接口
 *
 * 对应表：api_connections
 */
interface ApiConnectionRepository {

    /** 获取当前用户的所有 API 连接 */
    fun getAll(): Flow<Result<List<ApiConnection>>>

    /** 获取当前激活的 API 连接 */
    suspend fun getActive(): Result<ApiConnection?>

    /** 创建新连接 */
    suspend fun create(connection: ApiConnection): Result<ApiConnection>

    /** 更新连接 */
    suspend fun update(connection: ApiConnection): Result<ApiConnection>

    /** 删除连接 */
    suspend fun delete(connectionId: String): Result<Unit>

    /** 将指定连接设为当前 active（同一用户仅允许一个 active） */
    suspend fun setActive(connectionId: String): Result<Unit>
}
