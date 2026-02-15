package com.tenebralis.dreamos.domain.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 用户成就状态（PRD §14.1 — MVP 枚举）
 *
 * 适用表：user_achievements.status
 * SQL 默认值：'locked'
 */
@Serializable
enum class AchievementStatus {
    @SerialName("locked")      LOCKED,
    @SerialName("in_progress") IN_PROGRESS,
    @SerialName("unlocked")    UNLOCKED
}
