package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.ShopItemDto
import com.tenebralis.dreamos.domain.model.ShopItem

fun ShopItemDto.toDomain() = ShopItem(
    id = id,
    userId = userId,
    worldId = worldId,
    name = name,
    description = description,
    price = price,
    currencyCode = currencyCode,
    stock = stock,
    category = category,
    iconUrl = iconUrl,
    metadataJson = metadataJson,
    isActive = isActive,
    createdAt = createdAt
)

fun ShopItem.toDto() = ShopItemDto(
    id = id,
    userId = userId,
    worldId = worldId,
    name = name,
    description = description,
    price = price,
    currencyCode = currencyCode,
    stock = stock,
    category = category,
    iconUrl = iconUrl,
    metadataJson = metadataJson,
    isActive = isActive,
    createdAt = createdAt
)
