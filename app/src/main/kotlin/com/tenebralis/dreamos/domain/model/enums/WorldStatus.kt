package com.tenebralis.dreamos.domain.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 世界状态
 *
 * 适用表：worlds.status
 * SQL 默认值：'active'
 */
@Serializable
enum class WorldStatus {
    @SerialName("active")   ACTIVE,
    @SerialName("archived") ARCHIVED,
    @SerialName("deleted")  DELETED
}
