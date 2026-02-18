package com.tenebralis.dreamos.domain.model

import com.tenebralis.dreamos.domain.model.enums.AuthorType
import com.tenebralis.dreamos.domain.model.enums.CommentingMode
import com.tenebralis.dreamos.domain.model.enums.ForumVisibility

/**
 * 论坛帖子（领域模型）
 *
 * 对应表：forum_posts
 * author_type='npc' 时必须有 worldNpcPersonaId
 */
data class ForumPost(
    val id: String,
    val userId: String,
    val worldId: String,
    val authorType: AuthorType,
    val worldNpcPersonaId: String? = null,
    val title: String,
    val content: String,
    val visibility: ForumVisibility = ForumVisibility.WORLD,
    val commentingMode: CommentingMode = CommentingMode.ALL,
    val isPinned: Boolean = false,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
