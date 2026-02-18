package com.tenebralis.dreamos.data.remote.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * OpenAI 兼容 Chat Completions 响应体。
 */
@Serializable
data class ChatCompletionResponse(
    val id: String = "",
    val choices: List<ChatCompletionChoice> = emptyList(),
    val usage: ChatCompletionUsage? = null
)

@Serializable
data class ChatCompletionChoice(
    val index: Int = 0,
    val message: ChatMessage,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class ChatCompletionUsage(
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0,
    @SerialName("total_tokens") val totalTokens: Int = 0
)
