package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

/**
 * 用户设置仓库接口
 *
 * 对应表：user_settings
 */
interface UserSettingsRepository {

    /** 获取当前用户设置 */
    fun getSettings(): Flow<Result<UserSettings>>

    /** 更新用户设置 */
    suspend fun updateSettings(settings: UserSettings): Result<UserSettings>
}
