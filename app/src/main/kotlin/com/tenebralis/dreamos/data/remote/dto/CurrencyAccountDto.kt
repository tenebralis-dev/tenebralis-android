package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * currency_accounts 表 DTO
 */
@Serializable
data class CurrencyAccountDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("scope_type") val scopeType: String,
    @SerialName("world_id") val worldId: String? = null,
    @SerialName("currency_code") val currencyCode: String,
    val balance: Long = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
