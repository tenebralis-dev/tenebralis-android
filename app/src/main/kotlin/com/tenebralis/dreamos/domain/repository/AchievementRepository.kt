package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.Achievement
import com.tenebralis.dreamos.domain.model.UserAchievement
import com.tenebralis.dreamos.domain.model.enums.AchievementStatus
import kotlinx.coroutines.flow.Flow

/**
 * 成就仓库接口
 *
 * 对应表：achievements + user_achievements
 */
interface AchievementRepository {

    /** 获取成就定义列表（可按世界筛选） */
    fun getAchievements(worldId: String? = null): Flow<Result<List<Achievement>>>

    /** 获取当前用户的成就进度列表（可按状态筛选） */
    fun getUserAchievements(status: AchievementStatus? = null): Flow<Result<List<UserAchievement>>>

    /** 创建新成就定义 */
    suspend fun createAchievement(achievement: Achievement): Result<Achievement>

    /** 解锁成就 */
    suspend fun unlockAchievement(userAchievementId: String): Result<UserAchievement>

    /** 更新成就进度 */
    suspend fun updateProgress(userAchievementId: String, progressValue: Double): Result<UserAchievement>
}
