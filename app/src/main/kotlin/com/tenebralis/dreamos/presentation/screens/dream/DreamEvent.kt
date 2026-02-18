package com.tenebralis.dreamos.presentation.screens.dream

/**
 * 梦境 UI 事件
 */
sealed interface DreamEvent {
    /** 刷新消息列表 */
    data object Refresh : DreamEvent

    /** 发送行动描述 */
    data object Send : DreamEvent

    /** 重试上次发送失败的消息 */
    data object RetrySend : DreamEvent

    /** 重试 AI 调用 */
    data object RetryAiCall : DreamEvent

    /** 清除 AI 错误 */
    data object ClearAiError : DreamEvent

    /** 清除通用错误 */
    data object ClearError : DreamEvent

    /** 清除信息提示 */
    data object ClearInfo : DreamEvent

    /** 输入文本变化 */
    data class InputChanged(val text: String) : DreamEvent

    /** 切换上下文面板展开/折叠 */
    data object ToggleContext : DreamEvent
}
