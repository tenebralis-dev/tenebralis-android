package com.tenebralis.dreamos.presentation.screens.chat

sealed interface ChatDetailEvent {
    data object Refresh : ChatDetailEvent
    data class InputChanged(val value: String) : ChatDetailEvent
    data object Send : ChatDetailEvent
    data object RetrySend : ChatDetailEvent
    data object RetryAiCall : ChatDetailEvent
    data object ClearError : ChatDetailEvent
    data object ClearAiError : ChatDetailEvent
    data object ClearInfo : ChatDetailEvent
}
