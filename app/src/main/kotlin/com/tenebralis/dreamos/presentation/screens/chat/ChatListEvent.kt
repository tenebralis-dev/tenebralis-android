package com.tenebralis.dreamos.presentation.screens.chat

sealed interface ChatListEvent {
    data object Refresh : ChatListEvent
    data class SelectNpc(val npcId: String) : ChatListEvent
    data class OpenConversation(val conversationId: String) : ChatListEvent
    data object ConsumeNavigation : ChatListEvent
    data object ClearError : ChatListEvent
    data object ClearInfo : ChatListEvent
}
