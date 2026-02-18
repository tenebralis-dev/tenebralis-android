package com.tenebralis.dreamos.domain.service

import com.tenebralis.dreamos.data.remote.ai.ChatCompletionResponse
import com.tenebralis.dreamos.data.remote.ai.ChatMessage
import com.tenebralis.dreamos.domain.model.ApiConnection
import kotlinx.coroutines.flow.Flow

/**
 * AI Chat Completions 调用接口。
 *
 * 基于 OpenAI 兼容协议，支持非流式和流式两种模式。
 */
interface AiChatService {

    /**
     * 非流式调用 Chat Completions。
     *
     * @param connection active 连接配置（baseUrl/model/headers/params）
     * @param apiKey 解密后的 API Key
     * @param messages 已组装好的上下文消息列表
     * @return ChatCompletionResponse 包含 assistant 回复
     */
    suspend fun chatCompletion(
        connection: ApiConnection,
        apiKey: String,
        messages: List<ChatMessage>
    ): Result<ChatCompletionResponse>

    /**
     * 流式调用 Chat Completions（M4-P2 实现）。
     *
     * @return 逐块返回的 content 文本 Flow
     */
    fun chatCompletionStream(
        connection: ApiConnection,
        apiKey: String,
        messages: List<ChatMessage>
    ): Flow<Result<String>>
}
