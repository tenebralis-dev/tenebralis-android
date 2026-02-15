package com.tenebralis.dreamos.domain.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * AI 可见性规则（PRD §5.3）
 *
 * 适用表：user_notes, user_calendar, user_ledger, user_media,
 * pomodoro_sessions, global_memories
 */
@Serializable
enum class AiVisibility {
    @SerialName("private")       PRIVATE,
    @SerialName("assistant")     ASSISTANT,
    @SerialName("world_context") WORLD_CONTEXT,
    @SerialName("save_context")  SAVE_CONTEXT
}
