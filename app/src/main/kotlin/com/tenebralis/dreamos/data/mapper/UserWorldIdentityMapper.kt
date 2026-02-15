package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.UserWorldIdentityDto
import com.tenebralis.dreamos.domain.model.UserWorldIdentity

fun UserWorldIdentityDto.toDomain() = UserWorldIdentity(
    id = id,
    userId = userId,
    worldId = worldId,
    identityName = identityName,
    isActive = isActive,
    promptIdentityText = promptIdentityText,
    roleDataJson = roleDataJson,
    personaJson = personaJson,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UserWorldIdentity.toDto() = UserWorldIdentityDto(
    id = id,
    userId = userId,
    worldId = worldId,
    identityName = identityName,
    isActive = isActive,
    promptIdentityText = promptIdentityText,
    roleDataJson = roleDataJson,
    personaJson = personaJson,
    createdAt = createdAt,
    updatedAt = updatedAt
)
