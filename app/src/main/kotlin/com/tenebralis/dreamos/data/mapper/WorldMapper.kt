package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.WorldDto
import com.tenebralis.dreamos.domain.model.World

fun WorldDto.toDomain() = World(
    id = id,
    userId = userId,
    name = name,
    description = description,
    status = status,
    promptLoreText = promptLoreText,
    loreJson = loreJson,
    rulesJson = rulesJson,
    aiContextJson = aiContextJson,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun World.toDto() = WorldDto(
    id = id,
    userId = userId,
    name = name,
    description = description,
    status = status,
    promptLoreText = promptLoreText,
    loreJson = loreJson,
    rulesJson = rulesJson,
    aiContextJson = aiContextJson,
    createdAt = createdAt,
    updatedAt = updatedAt
)
