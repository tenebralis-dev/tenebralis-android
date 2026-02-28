package com.tenebralis.dreamos.presentation.screens.context

sealed interface ContextEvent {
    data class SelectTab(val index: Int) : ContextEvent
    data class UpdateRecentMessageCount(val count: Int) : ContextEvent
    data class UpdateMemoryTopN(val topN: Int) : ContextEvent
    data class UpdateMaxTokenEstimate(val max: Int) : ContextEvent
    data class ToggleLayer(val layerId: String) : ContextEvent
    data object ToggleAutoLog : ContextEvent
    data class UpdateLogRetentionDays(val days: Int) : ContextEvent
    data object CleanOldLogs : ContextEvent
    data object ClearAllLogs : ContextEvent
    data class FilterByConversation(val conversationId: String) : ContextEvent
    data object ClearFilter : ContextEvent
    data object ClearError : ContextEvent
    data object ClearInfo : ContextEvent
}
