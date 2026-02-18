package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.ForumCommentDto
import com.tenebralis.dreamos.domain.model.ForumComment
import com.tenebralis.dreamos.domain.model.enums.AuthorType

fun ForumCommentDto.toDomain() = ForumComment(
    id = id,
    userId = userId,
    postId = postId,
    parentCommentId = parentCommentId,
    authorType = AuthorType.entries.firstOrNull { it.name.equals(authorType, true) }
        ?: AuthorType.USER,
    worldNpcPersonaId = worldNpcPersonaId,
    content = content,
    likeCount = likeCount,
    createdAt = createdAt
)

fun ForumComment.toDto() = ForumCommentDto(
    id = id,
    userId = userId,
    postId = postId,
    parentCommentId = parentCommentId,
    authorType = authorType.name.lowercase(),
    worldNpcPersonaId = worldNpcPersonaId,
    content = content,
    likeCount = likeCount,
    createdAt = createdAt
)
