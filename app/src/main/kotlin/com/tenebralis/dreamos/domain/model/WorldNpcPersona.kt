package com.tenebralis.dreamos.domain.model

/**
 * NPC 世界人格（领域模型）
 *
 * 对应表：world_npc_personas
 * NPC 在特定世界中的角色设定，论坛 NPC 发言依赖此表
 */
data class WorldNpcPersona(
    val id: String,
    val userId: String,
    val worldId: String,
    val npcId: String,
    val personaJson: String = "{}",
    val displayName: String,
    val avatarUrl: String? = null,
    val promptText: String? = null,
    val createdAt: String? = null
)
