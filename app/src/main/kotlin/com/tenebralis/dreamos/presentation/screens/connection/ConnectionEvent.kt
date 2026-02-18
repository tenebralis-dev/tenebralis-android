package com.tenebralis.dreamos.presentation.screens.connection

import com.tenebralis.dreamos.domain.model.ServiceType

sealed interface ConnectionEvent {
    data object Refresh : ConnectionEvent
    data object StartCreate : ConnectionEvent
    data class EditConnection(val connectionId: String) : ConnectionEvent

    // ── 基本信息 ──
    data class NameChanged(val value: String) : ConnectionEvent
    data class ServiceTypeSelected(val value: ServiceType) : ConnectionEvent
    data class BaseUrlChanged(val value: String) : ConnectionEvent
    data class DefaultModelChanged(val value: String) : ConnectionEvent

    // ── 结构化 AI 参数 ──
    data class TemperatureChanged(val value: Float) : ConnectionEvent
    data class MaxTokensChanged(val value: String) : ConnectionEvent
    data class TopPChanged(val value: String) : ConnectionEvent
    data class FrequencyPenaltyChanged(val value: Float) : ConnectionEvent
    data class PresencePenaltyChanged(val value: Float) : ConnectionEvent
    data class StreamEnabledChanged(val value: Boolean) : ConnectionEvent

    // ── 高级配置 ──
    data class SystemPromptChanged(val value: String) : ConnectionEvent
    data class ParamsJsonOverrideChanged(val value: String) : ConnectionEvent
    data class HeadersTemplateJsonChanged(val value: String) : ConnectionEvent

    // ── 密钥 ──
    data class ApiKeyChanged(val value: String) : ConnectionEvent

    // ── 操作 ──
    data object Save : ConnectionEvent
    data object SetAsDefault : ConnectionEvent
    data object TestConnection : ConnectionEvent

    data class RequestDelete(val connectionId: String) : ConnectionEvent
    data object ConfirmDelete : ConnectionEvent
    data object DismissDeleteDialog : ConnectionEvent

    // ── 表单控制 ──
    data object HideForm : ConnectionEvent

    data object ClearInfo : ConnectionEvent
    data object ClearError : ConnectionEvent
}
