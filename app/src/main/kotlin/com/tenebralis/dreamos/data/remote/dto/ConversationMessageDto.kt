package com.tenebralis.dreamos.data.remote.dto

import com.tenebralis.dreamos.domain.model.enums.MessageRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * conversation_messages 表 DTO
 *
 * seq 单调递增；role 使用 MessageRole 枚举约束。
 */
@Serializable
data class ConversationMessageDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("conversation_id") val conversationId: String,
    val seq: Int,
    val role: MessageRole,
    val content: String,
    @SerialName("metadata_json") val metadataJson: JsonObject = JsonObject(emptyMap()),
    @SerialName("created_at") val createdAt: String? = null
)
