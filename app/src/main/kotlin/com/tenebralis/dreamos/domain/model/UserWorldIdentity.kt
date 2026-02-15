package com.tenebralis.dreamos.domain.model

import kotlinx.serialization.json.JsonObject

/**
 * 世界内身份（领域模型）
 *
 * 对应表：user_world_identities
 */
data class UserWorldIdentity(
    val id: String,
    val userId: String,
    val worldId: String,
    val identityName: String,
    val isActive: Boolean,
    val promptIdentityText: String?,
    val roleDataJson: JsonObject,
    val personaJson: JsonObject,
    val createdAt: String?,
    val updatedAt: String?
)
