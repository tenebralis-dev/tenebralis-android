package com.tenebralis.dreamos.presentation.screens.chat

import com.tenebralis.dreamos.data.remote.ai.ChatCompletionUsage
import com.tenebralis.dreamos.domain.model.AiPreset
import com.tenebralis.dreamos.domain.model.ApiConnection
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
    val streamingContent: String? = null,
    val infoMessage: String? = null,
    val lastAiUsage: ChatCompletionUsage? = null,
    /** 设置面板状态 */
    val showSettings: Boolean = false,
    val availablePresets: List<AiPreset> = emptyList(),
    val availableConnections: List<ApiConnection> = emptyList(),
    val currentPresetId: String? = null,
    val currentPresetName: String? = null,
    val currentConnectionId: String? = null,
    val currentConnectionName: String? = null
) {
    val emptyState: Boolean get() = !isLoading && messages.isEmpty()
}
