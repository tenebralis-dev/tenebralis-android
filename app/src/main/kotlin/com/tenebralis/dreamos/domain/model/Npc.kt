package com.tenebralis.dreamos.domain.model

import kotlinx.serialization.json.JsonObject

/**
 * NPC 定义（领域模型）
 *
 * 对应表：npcs
 */
data class Npc(
    val id: String,
    val userId: String,
    val name: String,
    val description: String?,
    val promptNpcText: String?,
    val personaJson: JsonObject,
    val createdAt: String?,
    val updatedAt: String?
)
