package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * user_npc_relationships 表 DTO
 */
@Serializable
data class RelationshipDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("world_id") val worldId: String,
    @SerialName("npc_id") val npcId: String,
    val affinity: Int = 0,
    val status: String = "neutral",
    @SerialName("flags_json") val flagsJson: String = "{}",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
