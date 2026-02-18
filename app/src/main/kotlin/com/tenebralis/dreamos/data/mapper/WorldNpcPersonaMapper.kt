package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.WorldNpcPersonaDto
import com.tenebralis.dreamos.domain.model.WorldNpcPersona

fun WorldNpcPersonaDto.toDomain() = WorldNpcPersona(
    id = id,
    userId = userId,
    worldId = worldId,
    npcId = npcId,
    personaJson = personaJson,
    displayName = displayName,
    avatarUrl = avatarUrl,
    promptText = promptText,
    createdAt = createdAt
)

fun WorldNpcPersona.toDto() = WorldNpcPersonaDto(
    id = id,
    userId = userId,
    worldId = worldId,
    npcId = npcId,
    personaJson = personaJson,
    displayName = displayName,
    avatarUrl = avatarUrl,
    promptText = promptText,
    createdAt = createdAt
)
