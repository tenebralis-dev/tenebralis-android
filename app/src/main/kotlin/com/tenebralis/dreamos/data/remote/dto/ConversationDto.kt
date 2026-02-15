package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * conversations 表 DTO
 *
 * 线程唯一定位：(user_id, save_id, npc_id, thread_key)
 */
@Serializable
data class ConversationDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("save_id") val saveId: String,
    @SerialName("npc_id") val npcId: String,
    @SerialName("thread_key") val threadKey: String,
    val title: String? = null,
    val summary: String? = null,
    @SerialName("pinned_context_text") val pinnedContextText: String? = null,
    @SerialName("last_message_at") val lastMessageAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
