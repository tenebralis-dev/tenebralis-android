package com.tenebralis.dreamos.domain.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 论坛帖子可见性（PRD §9）
 *
 * 适用表：forum_posts.visibility
 * SQL CHECK: visibility in ('world','private','archived')
 */
@Serializable
enum class ForumVisibility {
    @SerialName("world")    WORLD,
    @SerialName("private")  PRIVATE,
    @SerialName("archived") ARCHIVED
}
