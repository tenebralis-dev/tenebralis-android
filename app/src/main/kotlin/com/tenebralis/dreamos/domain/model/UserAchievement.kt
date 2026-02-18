package com.tenebralis.dreamos.domain.model

import com.tenebralis.dreamos.domain.model.enums.AchievementStatus

/**
 * 用户成就进度（领域模型）
 *
 * 对应表：user_achievements
 */
data class UserAchievement(
    val id: String,
    val userId: String,
    val achievementId: String,
    val scopeType: String,
    val saveId: String? = null,
    val status: AchievementStatus = AchievementStatus.LOCKED,
    val progressJson: String = "{}",
    val progressValue: Double = 0.0,
    val evidenceJson: String = "{}",
    val lastEvaluatedAt: String? = null,
    val unlockedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    /** 联查字段：关联的成就定义 */
    val achievement: Achievement? = null
)
