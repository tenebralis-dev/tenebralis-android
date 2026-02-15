package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.GlobalMemoryDto
import com.tenebralis.dreamos.domain.model.GlobalMemory

fun GlobalMemoryDto.toDomain() = GlobalMemory(
    id = id,
    userId = userId,
    memoryKey = memoryKey,
    content = content,
    summary = summary,
    memoryType = memoryType,
    aiVisibility = aiVisibility,
    importanceScore = importanceScore,
    confidenceScore = confidenceScore,
    sourceType = sourceType,
    sourceRefJson = sourceRefJson,
    tagsJson = tagsJson,
    metadataJson = metadataJson,
    isPinned = isPinned,
    isArchived = isArchived,
    recalledCount = recalledCount,
    lastRecalledAt = lastRecalledAt,
    expiresAt = expiresAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt
)

fun GlobalMemory.toDto() = GlobalMemoryDto(
    id = id,
    userId = userId,
    memoryKey = memoryKey,
    content = content,
    summary = summary,
    memoryType = memoryType,
    aiVisibility = aiVisibility,
    importanceScore = importanceScore,
    confidenceScore = confidenceScore,
    sourceType = sourceType,
    sourceRefJson = sourceRefJson,
    tagsJson = tagsJson,
    metadataJson = metadataJson,
    isPinned = isPinned,
    isArchived = isArchived,
    recalledCount = recalledCount,
    lastRecalledAt = lastRecalledAt,
    expiresAt = expiresAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt
)
