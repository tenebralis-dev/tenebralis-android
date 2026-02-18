package com.tenebralis.dreamos.domain.model

/**
 * 背包物品（领域模型）
 *
 * 对应表：user_inventory
 * 同一 item_id 多次购买 → quantity += 1（upsert）
 */
data class InventoryItem(
    val id: String,
    val userId: String,
    val itemId: String,
    val quantity: Int = 1,
    val acquiredAt: String? = null,
    val metadataJson: String = "{}",
    /** 关联的商品信息（join 查询时填充） */
    val shopItem: ShopItem? = null
)
