package com.tenebralis.dreamos.presentation.screens.context

import com.tenebralis.dreamos.domain.model.ContextLog
import com.tenebralis.dreamos.domain.model.ContextSettings

data class ContextUiState(
    val logs: List<ContextLog> = emptyList(),
    val settings: ContextSettings = ContextSettings(),
    val selectedTab: Int = 0,             // 0=日志, 1=设置
    val isLoading: Boolean = true,
    val logCount: Int = 0,
    val infoMessage: String? = null,
    val errorMessage: String? = null,
    val filterConversationId: String? = null
)
