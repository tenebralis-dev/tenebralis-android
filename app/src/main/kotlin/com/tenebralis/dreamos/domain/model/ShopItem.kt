package com.tenebralis.dreamos.domain.model

/**
 * 商品（领域模型）
 *
 * 对应表：shop_items
 * world_id = null 表示全局商店
 */
data class ShopItem(
    val id: String,
    val userId: String,
    val worldId: String? = null,
    val name: String,
    val description: String = "",
    val price: Long,
    val currencyCode: String = "points",
    val stock: Int? = null,
    val category: String = "",
    val iconUrl: String? = null,
    val metadataJson: String = "{}",
    val isActive: Boolean = true,
    val createdAt: String? = null
)
