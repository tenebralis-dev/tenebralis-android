package com.tenebralis.dreamos.domain.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 作用域类型（PRD §5.2）
 *
 * 适用表：tasks, user_tasks, achievements, user_achievements,
 * user_notes, user_calendar, user_ledger, user_media, pomodoro_sessions
 */
@Serializable
enum class ScopeType {
    @SerialName("global") GLOBAL,
    @SerialName("world")  WORLD,
    @SerialName("save")   SAVE
}
