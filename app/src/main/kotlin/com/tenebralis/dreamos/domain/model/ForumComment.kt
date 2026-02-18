package com.tenebralis.dreamos.domain.model

import com.tenebralis.dreamos.domain.model.enums.AuthorType

/**
 * 论坛评论（领域模型）
 *
 * 对应表：forum_comments
 * 支持楼中楼（parentCommentId）
 */
data class ForumComment(
    val id: String,
    val userId: String,
    val postId: String,
    val parentCommentId: String? = null,
    val authorType: AuthorType,
    val worldNpcPersonaId: String? = null,
    val content: String,
    val likeCount: Int = 0,
    val createdAt: String? = null
)
