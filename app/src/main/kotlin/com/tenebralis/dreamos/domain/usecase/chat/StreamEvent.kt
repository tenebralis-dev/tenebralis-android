package com.tenebralis.dreamos.domain.usecase.chat

import com.tenebralis.dreamos.domain.model.ConversationMessage

/**
 * 流式消息发送过程中的事件。
 *
 * ViewModel 通过收集 `Flow<StreamEvent>` 实时更新 UI 状态。
 */
sealed interface StreamEvent {

    /** user 消息已成功落库 */
    data class UserMessageSaved(val message: ConversationMessage) : StreamEvent

    /** AI 返回了一个新的文本 chunk，[textSoFar] 为截至目前的累积文本 */
    data class AiChunk(val textSoFar: String) : StreamEvent

    /** AI 流式生成完成，assistant 消息已落库 */
    data class AiCompleted(val assistant: ConversationMessage) : StreamEvent

    /** AI 调用失败（user 消息保留，assistant 消息不落库） */
    data class AiError(val error: String) : StreamEvent
}
