package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.UserDto
import com.tenebralis.dreamos.domain.model.User

fun UserDto.toDomain() = User(
    id = id,
    username = username,
    displayName = displayName,
    avatarUrl = avatarUrl,
    bio = bio,
    systemLevel = systemLevel,
    expPoints = expPoints,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun User.toDto() = UserDto(
    id = id,
    username = username,
    displayName = displayName,
    avatarUrl = avatarUrl,
    bio = bio,
    systemLevel = systemLevel,
    expPoints = expPoints,
    createdAt = createdAt,
    updatedAt = updatedAt
)
