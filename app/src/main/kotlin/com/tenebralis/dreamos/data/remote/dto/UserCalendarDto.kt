package com.tenebralis.dreamos.data.remote.dto

import com.tenebralis.dreamos.domain.model.enums.AiVisibility
import com.tenebralis.dreamos.domain.model.enums.ScopeType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * user_calendar 表 — 读取 DTO（完整对齐数据库列）
 */
@Serializable
data class UserCalendarDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("scope_type") val scopeType: ScopeType = ScopeType.GLOBAL,
    @SerialName("world_id") val worldId: String? = null,
    @SerialName("save_id") val saveId: String? = null,
    @SerialName("ai_visibility") val aiVisibility: AiVisibility = AiVisibility.PRIVATE,
    val title: String = "",
    val description: String? = null,
    @SerialName("start_at") val startAt: String,
    @SerialName("end_at") val endAt: String? = null,
    @SerialName("all_day") val allDay: Boolean = false,
    val timezone: String? = null,
    val rrule: String? = null,
    val location: String? = null,
    @SerialName("metadata_json") val metadataJson: JsonObject? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("deleted_at") val deletedAt: String? = null
)

/**
 * user_calendar 表 — 插入 DTO
 *
 * 排除服务端自动管理的列（created_at / updated_at / deleted_at）。
 */
@Serializable
data class UserCalendarInsertDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("scope_type") val scopeType: ScopeType = ScopeType.GLOBAL,
    @SerialName("world_id") val worldId: String? = null,
    @SerialName("save_id") val saveId: String? = null,
    @SerialName("ai_visibility") val aiVisibility: AiVisibility = AiVisibility.PRIVATE,
    val title: String = "",
    val description: String? = null,
    @SerialName("start_at") val startAt: String,
    @SerialName("end_at") val endAt: String? = null,
    @SerialName("all_day") val allDay: Boolean = false,
    val timezone: String? = null,
    val rrule: String? = null,
    val location: String? = null
)

/**
 * user_calendar 表 — 更新 DTO
 *
 * 不含 id / user_id（WHERE 条件里传），不含时间戳列。
 */
@Serializable
data class UserCalendarUpdateDto(
    val title: String = "",
    val description: String? = null,
    @SerialName("start_at") val startAt: String,
    @SerialName("end_at") val endAt: String? = null,
    @SerialName("all_day") val allDay: Boolean = false,
    val timezone: String? = null,
    val rrule: String? = null,
    val location: String? = null,
    @SerialName("scope_type") val scopeType: ScopeType = ScopeType.GLOBAL,
    @SerialName("world_id") val worldId: String? = null,
    @SerialName("save_id") val saveId: String? = null,
    @SerialName("ai_visibility") val aiVisibility: AiVisibility = AiVisibility.PRIVATE
)
