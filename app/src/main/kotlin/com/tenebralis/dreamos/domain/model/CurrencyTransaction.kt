package com.tenebralis.dreamos.domain.model

/**
 * 货币交易记录（领域模型）
 *
 * 对应表：currency_transactions
 * amount 正数=收入，负数=支出
 */
data class CurrencyTransaction(
    val id: String,
    val userId: String,
    val accountId: String,
    val worldId: String? = null,
    val amount: Long,
    val reasonType: String? = null,
    val reasonRef: String? = null,
    val metadataJson: String = "{}",
    val createdAt: String? = null
)
