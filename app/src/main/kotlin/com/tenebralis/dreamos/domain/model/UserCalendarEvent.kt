package com.tenebralis.dreamos.domain.model

import com.tenebralis.dreamos.domain.model.enums.AiVisibility
import com.tenebralis.dreamos.domain.model.enums.ScopeType
import kotlinx.serialization.json.JsonObject

/**
 * 用户日历事件（领域模型）
 *
 * 对应表：user_calendar
 */
data class UserCalendarEvent(
    val id: String,
    val userId: String,
    val title: String,
    val description: String? = null,
    val startAt: String,              // ISO 8601
    val endAt: String? = null,
    val allDay: Boolean = false,
    val timezone: String? = null,
    val rrule: String? = null,        // none / daily / weekly / monthly (or RRULE)
    val location: String? = null,
    val scopeType: ScopeType = ScopeType.GLOBAL,
    val worldId: String? = null,
    val saveId: String? = null,
    val aiVisibility: AiVisibility = AiVisibility.PRIVATE,
    val metadataJson: JsonObject? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val deletedAt: String? = null
)
