package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * 用户档案仓库接口
 *
 * 对应表：users
 */
interface UserRepository {

    /** 获取当前登录用户的档案 */
    fun getCurrentUser(): Flow<Result<User>>

    /** 更新用户档案 */
    suspend fun updateProfile(user: User): Result<User>
}
