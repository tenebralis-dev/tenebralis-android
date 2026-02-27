package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

/**
 * ai_presets 表 DTO
 */
@Serializable
data class AiPresetDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val name: String,
    @SerialName("preset_json") val presetJson: JsonObject = JsonObject(emptyMap()),
    @SerialName("regex_scripts_json") val regexScriptsJson: JsonArray = JsonArray(emptyList()),
    val source: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
