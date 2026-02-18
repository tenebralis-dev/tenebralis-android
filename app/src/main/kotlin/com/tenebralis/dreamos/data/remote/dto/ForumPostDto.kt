package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * forum_posts 表 DTO
 */
@Serializable
data class ForumPostDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("world_id") val worldId: String,
    @SerialName("author_type") val authorType: String,
    @SerialName("world_npc_persona_id") val worldNpcPersonaId: String? = null,
    val title: String,
    val content: String,
    val visibility: String = "world",
    @SerialName("commenting_mode") val commentingMode: String = "all",
    @SerialName("is_pinned") val isPinned: Boolean = false,
    @SerialName("like_count") val likeCount: Int = 0,
    @SerialName("comment_count") val commentCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
