package com.tenebralis.dreamos.data.remote.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * OpenAI 兼容 Chat Completions 流式响应 chunk。
 *
 * M4-P2 流式调用时使用。
 */
@Serializable
data class ChatCompletionStreamChunk(
    val id: String = "",
    val choices: List<StreamChoice> = emptyList()
)

@Serializable
data class StreamChoice(
    val index: Int = 0,
    val delta: StreamDelta = StreamDelta(),
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class StreamDelta(
    val role: String? = null,
    val content: String? = null
)
