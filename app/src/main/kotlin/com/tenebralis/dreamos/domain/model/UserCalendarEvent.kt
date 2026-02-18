package com.tenebralis.dreamos.domain.model

import com.tenebralis.dreamos.domain.model.enums.AiVisibility
import com.tenebralis.dreamos.domain.model.enums.ScopeType

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
    val isAllDay: Boolean = false,
    val repeatRule: String? = null,    // none / daily / weekly / monthly
    val scopeType: ScopeType = ScopeType.GLOBAL,
    val scopeId: String? = null,
    val aiVisibility: AiVisibility = AiVisibility.PRIVATE,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
