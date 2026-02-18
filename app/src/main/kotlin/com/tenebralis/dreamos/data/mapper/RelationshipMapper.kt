package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.RelationshipDto
import com.tenebralis.dreamos.domain.model.Relationship

fun RelationshipDto.toDomain(npcName: String? = null) = Relationship(
    id = id,
    userId = userId,
    worldId = worldId,
    npcId = npcId,
    affinity = affinity,
    status = status,
    flagsJson = flagsJson,
    createdAt = createdAt,
    updatedAt = updatedAt,
    npcName = npcName
)

fun Relationship.toDto() = RelationshipDto(
    id = id,
    userId = userId,
    worldId = worldId,
    npcId = npcId,
    affinity = affinity,
    status = status,
    flagsJson = flagsJson,
    createdAt = createdAt,
    updatedAt = updatedAt
)
