package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * forum_comments 表 DTO
 */
@Serializable
data class ForumCommentDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("post_id") val postId: String,
    @SerialName("parent_comment_id") val parentCommentId: String? = null,
    @SerialName("author_type") val authorType: String,
    @SerialName("world_npc_persona_id") val worldNpcPersonaId: String? = null,
    val content: String,
    @SerialName("like_count") val likeCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null
)
