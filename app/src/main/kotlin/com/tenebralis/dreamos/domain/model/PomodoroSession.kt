package com.tenebralis.dreamos.domain.model

import com.tenebralis.dreamos.domain.model.enums.AiVisibility
import com.tenebralis.dreamos.domain.model.enums.ScopeType

/**
 * 番茄钟会话（领域模型）
 *
 * 对应表：pomodoro_sessions
 */
data class PomodoroSession(
    val id: String,
    val userId: String,
    val startedAt: String,
    val endedAt: String? = null,
    val durationMinutes: Int = 25,
    val isCompleted: Boolean = false,
    val taskDescription: String? = null,
    val scopeType: ScopeType = ScopeType.GLOBAL,
    val scopeId: String? = null,
    val aiVisibility: AiVisibility = AiVisibility.ASSISTANT,
    val createdAt: String? = null
)
