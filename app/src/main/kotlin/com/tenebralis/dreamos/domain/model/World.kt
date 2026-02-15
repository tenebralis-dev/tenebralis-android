package com.tenebralis.dreamos.domain.model

import com.tenebralis.dreamos.domain.model.enums.WorldStatus
import kotlinx.serialization.json.JsonObject

/**
 * 世界定义（领域模型）
 *
 * 对应表：worlds
 */
data class World(
    val id: String,
    val userId: String,
    val name: String,
    val description: String?,
    val status: WorldStatus,
    val promptLoreText: String?,
    val loreJson: JsonObject,
    val rulesJson: JsonObject,
    val aiContextJson: JsonObject,
    val createdAt: String?,
    val updatedAt: String?
)
