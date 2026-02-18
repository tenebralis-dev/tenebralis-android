package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.AchievementDto
import com.tenebralis.dreamos.data.remote.dto.UserAchievementDto
import com.tenebralis.dreamos.domain.model.Achievement
import com.tenebralis.dreamos.domain.model.UserAchievement
import com.tenebralis.dreamos.domain.model.enums.AchievementStatus

fun AchievementDto.toDomain() = Achievement(
    id = id,
    userId = userId,
    worldId = worldId,
    name = name,
    description = description,
    scopeType = scopeType,
    promptAchievementText = promptAchievementText,
    criteriaJson = criteriaJson,
    createdSource = createdSource,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Achievement.toDto() = AchievementDto(
    id = id,
    userId = userId,
    worldId = worldId,
    name = name,
    description = description,
    scopeType = scopeType,
    promptAchievementText = promptAchievementText,
    criteriaJson = criteriaJson,
    createdSource = createdSource,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UserAchievementDto.toDomain(achievement: Achievement? = null) = UserAchievement(
    id = id,
    userId = userId,
    achievementId = achievementId,
    scopeType = scopeType,
    saveId = saveId,
    status = runCatching { AchievementStatus.valueOf(status.uppercase()) }
        .getOrDefault(AchievementStatus.LOCKED),
    progressJson = progressJson,
    progressValue = progressValue,
    evidenceJson = evidenceJson,
    lastEvaluatedAt = lastEvaluatedAt,
    unlockedAt = unlockedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    achievement = achievement
)

fun UserAchievement.toDto() = UserAchievementDto(
    id = id,
    userId = userId,
    achievementId = achievementId,
    scopeType = scopeType,
    saveId = saveId,
    status = status.name.lowercase(),
    progressJson = progressJson,
    progressValue = progressValue,
    evidenceJson = evidenceJson,
    lastEvaluatedAt = lastEvaluatedAt,
    unlockedAt = unlockedAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)
