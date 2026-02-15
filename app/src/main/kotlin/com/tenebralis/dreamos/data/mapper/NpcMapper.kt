package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.NpcDto
import com.tenebralis.dreamos.domain.model.Npc

fun NpcDto.toDomain() = Npc(
    id = id,
    userId = userId,
    name = name,
    description = description,
    promptNpcText = promptNpcText,
    personaJson = personaJson,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Npc.toDto() = NpcDto(
    id = id,
    userId = userId,
    name = name,
    description = description,
    promptNpcText = promptNpcText,
    personaJson = personaJson,
    createdAt = createdAt,
    updatedAt = updatedAt
)
