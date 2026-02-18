package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * shop_items 表 DTO
 */
@Serializable
data class ShopItemDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("world_id") val worldId: String? = null,
    val name: String,
    val description: String = "",
    val price: Long,
    @SerialName("currency_code") val currencyCode: String = "points",
    val stock: Int? = null,
    val category: String = "",
    @SerialName("icon_url") val iconUrl: String? = null,
    @SerialName("metadata_json") val metadataJson: String = "{}",
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)
