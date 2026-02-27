package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.AiPresetDto
import com.tenebralis.dreamos.domain.model.AiPreset

fun AiPresetDto.toDomain() = AiPreset(
    id = id,
    userId = userId,
    name = name,
    presetJson = presetJson,
    regexScriptsJson = regexScriptsJson,
    source = source,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun AiPreset.toDto() = AiPresetDto(
    id = id,
    userId = userId,
    name = name,
    presetJson = presetJson,
    regexScriptsJson = regexScriptsJson,
    source = source,
    createdAt = createdAt,
    updatedAt = updatedAt
)
