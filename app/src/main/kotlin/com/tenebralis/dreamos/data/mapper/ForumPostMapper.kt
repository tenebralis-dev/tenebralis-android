package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.ForumPostDto
import com.tenebralis.dreamos.domain.model.ForumPost
import com.tenebralis.dreamos.domain.model.enums.AuthorType
import com.tenebralis.dreamos.domain.model.enums.CommentingMode
import com.tenebralis.dreamos.domain.model.enums.ForumVisibility

fun ForumPostDto.toDomain() = ForumPost(
    id = id,
    userId = userId,
    worldId = worldId,
    authorType = AuthorType.entries.firstOrNull { it.name.equals(authorType, true) }
        ?: AuthorType.USER,
    worldNpcPersonaId = worldNpcPersonaId,
    title = title,
    content = content,
    visibility = ForumVisibility.entries.firstOrNull { it.name.equals(visibility, true) }
        ?: ForumVisibility.WORLD,
    commentingMode = CommentingMode.entries.firstOrNull { it.name.equals(commentingMode, true) }
        ?: CommentingMode.ALL,
    isPinned = isPinned,
    likeCount = likeCount,
    commentCount = commentCount,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ForumPost.toDto() = ForumPostDto(
    id = id,
    userId = userId,
    worldId = worldId,
    authorType = authorType.name.lowercase(),
    worldNpcPersonaId = worldNpcPersonaId,
    title = title,
    content = content,
    visibility = visibility.name.lowercase(),
    commentingMode = when (commentingMode) {
        CommentingMode.ALL -> "all"
        CommentingMode.AI_ONLY -> "ai_only"
        CommentingMode.USER_ONLY -> "user_only"
    },
    isPinned = isPinned,
    likeCount = likeCount,
    commentCount = commentCount,
    createdAt = createdAt,
    updatedAt = updatedAt
)
