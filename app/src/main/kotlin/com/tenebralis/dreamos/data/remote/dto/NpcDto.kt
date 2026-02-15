package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * npcs 表 DTO
 */
@Serializable
data class NpcDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val name: String,
    val description: String? = null,
    @SerialName("prompt_npc_text") val promptNpcText: String? = null,
    @SerialName("persona_json") val personaJson: JsonObject = JsonObject(emptyMap()),
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
