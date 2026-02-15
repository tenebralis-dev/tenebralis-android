package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * user_world_identities 表 DTO
 */
@Serializable
data class UserWorldIdentityDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("world_id") val worldId: String,
    @SerialName("identity_name") val identityName: String,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("prompt_identity_text") val promptIdentityText: String? = null,
    @SerialName("role_data_json") val roleDataJson: JsonObject = JsonObject(emptyMap()),
    @SerialName("persona_json") val personaJson: JsonObject = JsonObject(emptyMap()),
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
