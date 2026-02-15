package com.tenebralis.dreamos.data.remote.dto

import com.tenebralis.dreamos.domain.model.enums.WorldStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * worlds 表 DTO
 */
@Serializable
data class WorldDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val name: String,
    val description: String? = null,
    val status: WorldStatus = WorldStatus.ACTIVE,
    @SerialName("prompt_lore_text") val promptLoreText: String? = null,
    @SerialName("lore_json") val loreJson: JsonObject = JsonObject(emptyMap()),
    @SerialName("rules_json") val rulesJson: JsonObject = JsonObject(emptyMap()),
    @SerialName("ai_context_json") val aiContextJson: JsonObject = JsonObject(emptyMap()),
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
