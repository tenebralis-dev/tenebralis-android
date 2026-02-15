package com.tenebralis.dreamos.domain.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 货币账户作用域（PRD §9）
 *
 * 适用表：currency_accounts.scope_type
 * SQL CHECK: scope_type in ('global','world')
 * 注意：仅 global/world 两种，不含 save
 */
@Serializable
enum class CurrencyScopeType {
    @SerialName("global") GLOBAL,
    @SerialName("world")  WORLD
}
