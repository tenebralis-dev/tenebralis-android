package com.tenebralis.dreamos.data.remote.dto

import com.tenebralis.dreamos.domain.model.enums.AiVisibility
import com.tenebralis.dreamos.domain.model.enums.ScopeType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * pomodoro_sessions 表 DTO
 */
@Serializable
data class PomodoroSessionDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("started_at") val startedAt: String,
    @SerialName("ended_at") val endedAt: String? = null,
    @SerialName("duration_minutes") val durationMinutes: Int = 25,
    @SerialName("is_completed") val isCompleted: Boolean = false,
    @SerialName("task_description") val taskDescription: String? = null,
    @SerialName("scope_type") val scopeType: ScopeType = ScopeType.GLOBAL,
    @SerialName("scope_id") val scopeId: String? = null,
    @SerialName("ai_visibility") val aiVisibility: AiVisibility = AiVisibility.ASSISTANT,
    @SerialName("created_at") val createdAt: String? = null
)
