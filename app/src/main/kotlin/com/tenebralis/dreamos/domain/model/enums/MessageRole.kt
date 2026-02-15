package com.tenebralis.dreamos.domain.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 对话消息角色（PRD §9）
 *
 * 适用表：conversation_messages.role
 * SQL CHECK: role in ('user','assistant','system','tool')
 */
@Serializable
enum class MessageRole {
    @SerialName("user")      USER,
    @SerialName("assistant") ASSISTANT,
    @SerialName("system")    SYSTEM,
    @SerialName("tool")      TOOL
}
