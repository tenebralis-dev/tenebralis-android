package com.tenebralis.dreamos.presentation.screens.connection

import com.tenebralis.dreamos.domain.model.ApiConnection
import com.tenebralis.dreamos.domain.usecase.connection.ConnectionTestResult

data class ConnectionFormState(
    val name: String = "",
    val serviceType: String = "openai_compat",
    val baseUrl: String = "",
    val defaultModel: String = "",
    val systemPrompt: String = "",
    val paramsJson: String = "{}",
    val headersTemplateJson: String = "{}",
    val apiKey: String = ""
)

data class ConnectionUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isTesting: Boolean = false,
    val isSettingActive: Boolean = false,
    val connections: List<ApiConnection> = emptyList(),
    val editingConnectionId: String? = null,
    val pendingDeleteConnectionId: String? = null,
    val form: ConnectionFormState = ConnectionFormState(),
    val testResult: ConnectionTestResult? = null,
    val infoMessage: String? = null,
    val errorMessage: String? = null
) {
    val isEditing: Boolean get() = editingConnectionId != null
}
