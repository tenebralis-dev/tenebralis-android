package com.tenebralis.dreamos.domain.model

import kotlinx.serialization.json.JsonObject

/**
 * 存档状态（领域模型）
 *
 * 对应表：world_save_states
 */
data class WorldSaveState(
    val id: String,
    val userId: String,
    val worldId: String,
    val identityId: String,
    val slot: Int,
    val title: String?,
    val summary: String?,
    val chapter: String?,
    val stage: String?,
    val promptProgressText: String?,
    val stateJson: JsonObject,
    val lastPlayedAt: String?,
    val createdAt: String?,
    val updatedAt: String?
)
