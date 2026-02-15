package com.tenebralis.dreamos.domain.model

/**
 * 会话线程（领域模型）
 *
 * 对应表：conversations
 * 唯一定位：(userId, saveId, npcId, threadKey)
 */
data class Conversation(
    val id: String,
    val userId: String,
    val saveId: String,
    val npcId: String,
    val threadKey: String,
    val title: String?,
    val summary: String?,
    val pinnedContextText: String?,
    val lastMessageAt: String?,
    val createdAt: String?,
    val updatedAt: String?
)
