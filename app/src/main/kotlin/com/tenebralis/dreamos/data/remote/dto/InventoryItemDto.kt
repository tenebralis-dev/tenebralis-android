package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * user_inventory 表 DTO
 */
@Serializable
data class InventoryItemDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("item_id") val itemId: String,
    val quantity: Int = 1,
    @SerialName("acquired_at") val acquiredAt: String? = null,
    @SerialName("metadata_json") val metadataJson: String = "{}"
)
