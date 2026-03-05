package com.tenebralis.dreamos.presentation.screens.chat

sealed interface ChatDetailEvent {
    data object Refresh : ChatDetailEvent
    data class InputChanged(val value: String) : ChatDetailEvent
    data object Send : ChatDetailEvent
    data object RetrySend : ChatDetailEvent
    data object RetryAiCall : ChatDetailEvent
    data object ClearError : ChatDetailEvent
    data object ClearAiError : ChatDetailEvent
    data object StopStreaming : ChatDetailEvent
    data object ClearInfo : ChatDetailEvent

    /** 设置面板事件 */
    data object ShowSettings : ChatDetailEvent
    data object DismissSettings : ChatDetailEvent
    data class ChangePreset(val presetId: String?) : ChatDetailEvent
    data class ChangeApiConnection(val connectionId: String?) : ChatDetailEvent
}
