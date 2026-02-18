package com.tenebralis.dreamos.presentation.screens.connection

import com.tenebralis.dreamos.domain.model.ApiConnection
import com.tenebralis.dreamos.domain.model.ServiceType
import com.tenebralis.dreamos.domain.usecase.connection.ConnectionTestResult

data class ConnectionFormState(
    val name: String = "",
    val serviceType: ServiceType = ServiceType.OPENAI_COMPAT,
    val baseUrl: String = "",
    val defaultModel: String = "",
    val systemPrompt: String = "",
    // 结构化 AI 参数（Slider / 数字输入）
    val temperature: Float = 0.7f,
    val maxTokens: String = "",           // 空 = 不设置
    val topP: String = "",                // 空 = 不设置
    val frequencyPenalty: Float = 0.0f,
    val presencePenalty: Float = 0.0f,
    val streamEnabled: Boolean = true,
    // 高级：原始 JSON 覆盖
    val paramsJsonOverride: String = "",  // 非空时覆盖上方结构化参数
    val headersTemplateJson: String = "{}",
    // 密钥
    val apiKey: String = "",
    val hasExistingApiKey: Boolean = false,
    val existingApiKeyMask: String = "",  // "sk-...xxxx"
    // 实时校验
    val baseUrlError: String? = null,
    val nameError: String? = null,
    val paramsJsonError: String? = null,
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
