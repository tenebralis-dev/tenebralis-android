package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.InventoryItemDto
import com.tenebralis.dreamos.domain.model.InventoryItem

fun InventoryItemDto.toDomain() = InventoryItem(
    id = id,
    userId = userId,
    itemId = itemId,
    quantity = quantity,
    acquiredAt = acquiredAt,
    metadataJson = metadataJson
)

fun InventoryItem.toDto() = InventoryItemDto(
    id = id,
    userId = userId,
    itemId = itemId,
    quantity = quantity,
    acquiredAt = acquiredAt,
    metadataJson = metadataJson
)
