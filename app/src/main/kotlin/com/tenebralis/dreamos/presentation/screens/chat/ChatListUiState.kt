package com.tenebralis.dreamos.presentation.screens.chat

import com.tenebralis.dreamos.domain.model.Conversation
import com.tenebralis.dreamos.domain.model.Npc

data class ChatListUiState(
    val saveId: String? = null,
    val isLoading: Boolean = false,
    val isCreatingConversation: Boolean = false,
    val selectedNpcId: String? = null,
    val conversations: List<Conversation> = emptyList(),
    val npcs: List<Npc> = emptyList(),
    val navigateToConversationId: String? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null
) {
    val requiresSaveSelection: Boolean get() = saveId.isNullOrBlank()
    val emptyConversationState: Boolean get() = !requiresSaveSelection && !isLoading && conversations.isEmpty()
    val emptyNpcState: Boolean get() = !requiresSaveSelection && !isLoading && npcs.isEmpty()
}
