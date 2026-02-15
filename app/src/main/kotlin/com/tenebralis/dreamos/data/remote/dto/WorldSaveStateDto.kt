package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * world_save_states 表 DTO
 */
@Serializable
data class WorldSaveStateDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("world_id") val worldId: String,
    @SerialName("identity_id") val identityId: String,
    val slot: Int,
    val title: String? = null,
    val summary: String? = null,
    val chapter: String? = null,
    val stage: String? = null,
    @SerialName("prompt_progress_text") val promptProgressText: String? = null,
    @SerialName("state_json") val stateJson: JsonObject = JsonObject(emptyMap()),
    @SerialName("last_played_at") val lastPlayedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
