package com.tenebralis.dreamos.presentation.screens.chat

import com.tenebralis.dreamos.data.remote.ai.ChatCompletionUsage
import com.tenebralis.dreamos.domain.model.ConversationMessage

data class ChatDetailUiState(
    val conversationId: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isAiResponding: Boolean = false,
    val messages: List<ConversationMessage> = emptyList(),
    val inputText: String = "",
    val failedContent: String? = null,
    val errorMessage: String? = null,
    val aiErrorMessage: String? = null,
    val infoMessage: String? = null,
    val lastAiUsage: ChatCompletionUsage? = null
) {
    val emptyState: Boolean get() = !isLoading && messages.isEmpty()
}
