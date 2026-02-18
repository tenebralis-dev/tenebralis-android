package com.tenebralis.dreamos.data.remote.dto

import com.tenebralis.dreamos.domain.model.enums.AiVisibility
import com.tenebralis.dreamos.domain.model.enums.ScopeType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * user_calendar 表 DTO
 */
@Serializable
data class UserCalendarDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String = "",
    val description: String? = null,
    @SerialName("start_at") val startAt: String,
    @SerialName("end_at") val endAt: String? = null,
    @SerialName("is_all_day") val isAllDay: Boolean = false,
    @SerialName("repeat_rule") val repeatRule: String? = null,
    @SerialName("scope_type") val scopeType: ScopeType = ScopeType.GLOBAL,
    @SerialName("scope_id") val scopeId: String? = null,
    @SerialName("ai_visibility") val aiVisibility: AiVisibility = AiVisibility.PRIVATE,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
