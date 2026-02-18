package com.tenebralis.dreamos.domain.model

/**
 * 成就定义（领域模型）
 *
 * 对应表：achievements
 */
data class Achievement(
    val id: String,
    val userId: String,
    val worldId: String? = null,
    val name: String,
    val description: String? = null,
    val scopeType: String,
    val promptAchievementText: String? = null,
    val criteriaJson: String = "{}",
    val createdSource: String = "manual",
    val createdAt: String? = null,
    val updatedAt: String? = null
)
