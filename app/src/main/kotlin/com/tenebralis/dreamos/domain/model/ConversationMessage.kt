package com.tenebralis.dreamos.domain.model

import com.tenebralis.dreamos.domain.model.enums.MessageRole
import kotlinx.serialization.json.JsonObject

/**
 * 消息记录（领域模型）
 *
 * 对应表：conversation_messages
 */
data class ConversationMessage(
    val id: String,
    val userId: String,
    val conversationId: String,
    val seq: Int,
    val role: MessageRole,
    val content: String,
    val metadataJson: JsonObject,
    val createdAt: String?
)
