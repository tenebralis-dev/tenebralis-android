package com.tenebralis.dreamos.domain.model

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

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

/**
 * 从 persona_json 中提取头像 URL（如果存在）
 */
val Npc.avatarUrl: String?
    get() = personaJson["avatar_url"]
        ?.jsonPrimitive?.contentOrNull
        ?.takeIf { it.isNotBlank() }

/**
 * 从 persona_json 中提取头像存储路径（Supabase Storage 桶内路径）
 */
val Npc.avatarPath: String?
    get() = personaJson["avatar_path"]
        ?.jsonPrimitive?.contentOrNull
        ?.takeIf { it.isNotBlank() }
