package com.tenebralis.dreamos.data.remote.ai

import kotlinx.serialization.Serializable

/**
 * AI API 请求/响应中的消息结构。
 *
 * 不是数据库模型，仅用于与 OpenAI 兼容 API 交互。
 */
@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)
