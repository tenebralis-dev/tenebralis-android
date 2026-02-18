package com.tenebralis.dreamos.domain.usecase.chat

import com.tenebralis.dreamos.domain.model.ConversationMessage

/**
 * AI 消息发送结果。
 *
 * @param userMessage     用户消息（始终落库）
 * @param assistantMessage AI 回复消息（AI 失败时为 null）
 * @param aiError         AI 调用失败时的错误描述（成功时为 null）
 */
data class SendMessageResult(
    val userMessage: ConversationMessage,
    val assistantMessage: ConversationMessage?,
    val aiError: String? = null
)
