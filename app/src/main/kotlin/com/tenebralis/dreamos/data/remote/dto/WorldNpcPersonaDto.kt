package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * world_npc_personas 表 DTO
 *
 * NPC 在特定世界中的角色设定
 */
@Serializable
data class WorldNpcPersonaDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("world_id") val worldId: String,
    @SerialName("npc_id") val npcId: String,
    @SerialName("persona_json") val personaJson: String = "{}",
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("prompt_text") val promptText: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
