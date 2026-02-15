package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.WorldSaveStateDto
import com.tenebralis.dreamos.domain.model.WorldSaveState

fun WorldSaveStateDto.toDomain() = WorldSaveState(
    id = id,
    userId = userId,
    worldId = worldId,
    identityId = identityId,
    slot = slot,
    title = title,
    summary = summary,
    chapter = chapter,
    stage = stage,
    promptProgressText = promptProgressText,
    stateJson = stateJson,
    lastPlayedAt = lastPlayedAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun WorldSaveState.toDto() = WorldSaveStateDto(
    id = id,
    userId = userId,
    worldId = worldId,
    identityId = identityId,
    slot = slot,
    title = title,
    summary = summary,
    chapter = chapter,
    stage = stage,
    promptProgressText = promptProgressText,
    stateJson = stateJson,
    lastPlayedAt = lastPlayedAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)
