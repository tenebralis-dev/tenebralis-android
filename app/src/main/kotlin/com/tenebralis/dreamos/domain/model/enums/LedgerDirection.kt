package com.tenebralis.dreamos.domain.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 账本方向（PRD §9）
 *
 * 适用表：user_ledger.direction
 * SQL CHECK: direction in ('income','expense','transfer')
 */
@Serializable
enum class LedgerDirection {
    @SerialName("income")   INCOME,
    @SerialName("expense")  EXPENSE,
    @SerialName("transfer") TRANSFER
}
