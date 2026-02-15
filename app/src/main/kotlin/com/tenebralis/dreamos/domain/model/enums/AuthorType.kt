package com.tenebralis.dreamos.domain.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 论坛作者类型
 *
 * 适用表：forum_posts.author_type, forum_comments.author_type
 * SQL CHECK: author_type = 'npc' 时必须有 world_npc_persona_id
 */
@Serializable
enum class AuthorType {
    @SerialName("user")     USER,
    @SerialName("npc")      NPC,
    @SerialName("identity") IDENTITY
}
