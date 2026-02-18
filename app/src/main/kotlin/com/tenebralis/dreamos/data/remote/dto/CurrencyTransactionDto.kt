package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * currency_transactions 表 DTO
 */
@Serializable
data class CurrencyTransactionDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("account_id") val accountId: String,
    @SerialName("world_id") val worldId: String? = null,
    val amount: Long,
    @SerialName("reason_type") val reasonType: String? = null,
    @SerialName("reason_ref") val reasonRef: String? = null,
    @SerialName("metadata_json") val metadataJson: String = "{}",
    @SerialName("created_at") val createdAt: String? = null
)
