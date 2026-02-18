package com.tenebralis.dreamos.domain.model

/**
 * 货币账户（领域模型）
 *
 * 对应表：currency_accounts
 * scope_type: "global"（积分）| "world"（世界货币）
 */
data class CurrencyAccount(
    val id: String,
    val userId: String,
    val scopeType: String,
    val worldId: String? = null,
    val currencyCode: String,
    val balance: Long = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
