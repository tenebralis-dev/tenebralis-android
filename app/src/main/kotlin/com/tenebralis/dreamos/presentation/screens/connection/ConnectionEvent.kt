package com.tenebralis.dreamos.presentation.screens.connection

sealed interface ConnectionEvent {
    data object Refresh : ConnectionEvent
    data object StartCreate : ConnectionEvent
    data class EditConnection(val connectionId: String) : ConnectionEvent

    data class NameChanged(val value: String) : ConnectionEvent
    data class ServiceTypeChanged(val value: String) : ConnectionEvent
    data class BaseUrlChanged(val value: String) : ConnectionEvent
    data class DefaultModelChanged(val value: String) : ConnectionEvent
    data class SystemPromptChanged(val value: String) : ConnectionEvent
    data class ParamsJsonChanged(val value: String) : ConnectionEvent
    data class HeadersTemplateJsonChanged(val value: String) : ConnectionEvent
    data class ApiKeyChanged(val value: String) : ConnectionEvent

    data object Save : ConnectionEvent
    data object SetAsDefault : ConnectionEvent
    data object TestConnection : ConnectionEvent

    data class RequestDelete(val connectionId: String) : ConnectionEvent
    data object ConfirmDelete : ConnectionEvent
    data object DismissDeleteDialog : ConnectionEvent

    data object ClearInfo : ConnectionEvent
    data object ClearError : ConnectionEvent
}
