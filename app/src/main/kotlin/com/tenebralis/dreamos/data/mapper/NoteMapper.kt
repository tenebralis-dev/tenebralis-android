package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.UserNoteDto
import com.tenebralis.dreamos.domain.model.UserNote

fun UserNoteDto.toDomain() = UserNote(
    id = id,
    userId = userId,
    title = title,
    content = content,
    tags = tags,
    scopeType = scopeType,
    scopeId = scopeId,
    aiVisibility = aiVisibility,
    isPinned = isPinned,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UserNote.toDto() = UserNoteDto(
    id = id,
    userId = userId,
    title = title,
    content = content,
    tags = tags,
    scopeType = scopeType,
    scopeId = scopeId,
    aiVisibility = aiVisibility,
    isPinned = isPinned,
    createdAt = createdAt,
    updatedAt = updatedAt
)
