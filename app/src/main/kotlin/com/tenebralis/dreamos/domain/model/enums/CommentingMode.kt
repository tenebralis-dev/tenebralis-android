package com.tenebralis.dreamos.domain.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 论坛帖子评论模式（PRD §9）
 *
 * 适用表：forum_posts.commenting_mode
 * SQL CHECK: commenting_mode in ('all','ai_only','user_only')
 */
@Serializable
enum class CommentingMode {
    @SerialName("all")       ALL,
    @SerialName("ai_only")   AI_ONLY,
    @SerialName("user_only") USER_ONLY
}
