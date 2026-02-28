package com.tenebralis.dreamos.domain.model

/**
 * 上下文中的单层内容
 */
data class ContextLayer(
    val enabled: Boolean,
    val content: String?,
    val tokens: Int,
    /** 可选：记忆条数 / 消息条数 */
    val count: Int? = null
)

/**
 * 上下文日志 Domain Model
 *
 * 记录单次 AI 调用时组装的完整上下文快照。
 */
data class ContextLog(
    val id: Long = 0,
    val conversationId: String,
    val createdAt: String,
    val totalTokensEstimate: Int,
    val layers: Map<String, ContextLayer>,
    val fullPromptText: String
)
