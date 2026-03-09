package com.tenebralis.dreamos.presentation.screens.connection

import com.tenebralis.dreamos.domain.model.ApiConnection
import com.tenebralis.dreamos.domain.model.ServiceType
import com.tenebralis.dreamos.domain.usecase.connection.ConnectionTestResult

data class ConnectionFormState(
    val name: String = "",
    val serviceType: ServiceType = ServiceType.OPENAI_COMPAT,
    val baseUrl: String = "",
    val defaultModel: String = "",
    val headersTemplateJson: String = "{}",
    // 解析后端点展示
    val resolvedBaseUrl: String = "",
    val modelsEndpoint: String = "",
    val chatEndpoint: String = "",
    // 密钥
    val apiKey: String = "",
    val hasExistingApiKey: Boolean = false,
    val existingApiKeyMask: String = "",  // "sk-...xxxx"
    // 模型列表
    val availableModels: List<String> = emptyList(),
    val isFetchingModels: Boolean = false,
    // 实时校验
    val baseUrlError: String? = null,
    val nameError: String? = null,
    val headersJsonError: String? = null,
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
    val errorMessage: String? = null,
    // BottomSheet 控制
    val isFormVisible: Boolean = false,
) {
    val isEditing: Boolean get() = editingConnectionId != null
}
