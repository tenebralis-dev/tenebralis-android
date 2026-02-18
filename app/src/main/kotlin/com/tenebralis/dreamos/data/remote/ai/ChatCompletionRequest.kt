package com.tenebralis.dreamos.data.remote.ai

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * OpenAI 兼容 Chat Completions 请求体。
 *
 * 可选字段使用 `@EncodeDefault(NEVER)` 确保为 null 时不序列化，
 * 避免代理/提供商对 null 值报 validation error。
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @SerialName("max_tokens") val maxTokens: Int? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @SerialName("top_p") val topP: Double? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @SerialName("frequency_penalty") val frequencyPenalty: Double? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @SerialName("presence_penalty") val presencePenalty: Double? = null,
    val stream: Boolean = false
)
