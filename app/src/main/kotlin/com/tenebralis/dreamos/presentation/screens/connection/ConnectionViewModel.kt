package com.tenebralis.dreamos.presentation.screens.connection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.model.ApiConnection
import com.tenebralis.dreamos.domain.model.ServiceType
import com.tenebralis.dreamos.domain.usecase.connection.ConnectionDraft
import com.tenebralis.dreamos.domain.usecase.connection.CreateConnectionUseCase
import com.tenebralis.dreamos.domain.usecase.connection.DeleteConnectionUseCase
import com.tenebralis.dreamos.domain.usecase.connection.FetchModelsUseCase
import com.tenebralis.dreamos.domain.usecase.connection.GetConnectionSecretUseCase
import com.tenebralis.dreamos.domain.usecase.connection.GetConnectionsUseCase
import com.tenebralis.dreamos.domain.usecase.connection.SaveConnectionSecretUseCase
import com.tenebralis.dreamos.domain.usecase.connection.SetActiveConnectionUseCase
import com.tenebralis.dreamos.domain.usecase.connection.TestConnectionUseCase
import com.tenebralis.dreamos.domain.usecase.connection.UpdateConnectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.URI
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val getConnectionsUseCase: GetConnectionsUseCase,
    private val createConnectionUseCase: CreateConnectionUseCase,
    private val updateConnectionUseCase: UpdateConnectionUseCase,
    private val deleteConnectionUseCase: DeleteConnectionUseCase,
    private val setActiveConnectionUseCase: SetActiveConnectionUseCase,
    private val saveConnectionSecretUseCase: SaveConnectionSecretUseCase,
    private val getConnectionSecretUseCase: GetConnectionSecretUseCase,
    private val testConnectionUseCase: TestConnectionUseCase,
    private val fetchModelsUseCase: FetchModelsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectionUiState())
    val uiState: StateFlow<ConnectionUiState> = _uiState.asStateFlow()

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    init {
        refreshConnections()
    }

    fun onEvent(event: ConnectionEvent) {
        when (event) {
            ConnectionEvent.Refresh -> refreshConnections()
            ConnectionEvent.StartCreate -> startCreateMode()
            is ConnectionEvent.StartCreateWithPreset -> startCreateWithPreset(event.serviceType)
            is ConnectionEvent.EditConnection -> startEditMode(event.connectionId)

            // ── 基本信息 ──
            is ConnectionEvent.NameChanged ->
                updateForm { copy(name = event.value, nameError = null) }

            is ConnectionEvent.ServiceTypeSelected -> {
                val form = _uiState.value.form
                val newBaseUrl = if (form.baseUrl.isBlank() || isDefaultBaseUrl(form.baseUrl)) {
                    event.value.defaultBaseUrl.orEmpty()
                } else {
                    form.baseUrl
                }
                val sanitized = sanitizeBaseUrl(newBaseUrl)
                updateForm {
                    copy(
                        serviceType = event.value,
                        baseUrl = newBaseUrl,
                        baseUrlError = null,
                        resolvedBaseUrl = sanitized,
                        modelsEndpoint = if (sanitized.isNotBlank()) "$sanitized/v1/models" else "",
                        chatEndpoint = if (sanitized.isNotBlank()) "$sanitized/v1/chat/completions" else ""
                    )
                }
            }

            is ConnectionEvent.BaseUrlChanged -> {
                val sanitized = sanitizeBaseUrl(event.value)
                updateForm {
                    copy(
                        baseUrl = event.value,
                        baseUrlError = validateBaseUrlInstant(event.value),
                        name = if (name.isBlank()) suggestName(event.value) else name,
                        resolvedBaseUrl = sanitized,
                        modelsEndpoint = if (sanitized.isNotBlank()) "$sanitized/v1/models" else "",
                        chatEndpoint = if (sanitized.isNotBlank()) "$sanitized/v1/chat/completions" else ""
                    )
                }
            }

            is ConnectionEvent.DefaultModelChanged ->
                updateForm { copy(defaultModel = event.value) }

            // ── 高级配置 ──
            is ConnectionEvent.HeadersTemplateJsonChanged ->
                updateForm {
                    copy(
                        headersTemplateJson = event.value,
                        headersJsonError = validateJsonAndHeaders(event.value)
                    )
                }

            // ── 密钥 ──
            is ConnectionEvent.ApiKeyChanged ->
                updateForm { copy(apiKey = event.value) }

            // ── 操作 ──
            ConnectionEvent.Save -> saveConnection()
            ConnectionEvent.SetAsDefault -> setAsDefault()
            ConnectionEvent.TestConnection -> testConnection()
            ConnectionEvent.FetchModels -> fetchModels()

            is ConnectionEvent.RequestDelete ->
                _uiState.update { it.copy(pendingDeleteConnectionId = event.connectionId) }

            ConnectionEvent.ConfirmDelete -> confirmDelete()
            ConnectionEvent.DismissDeleteDialog ->
                _uiState.update { it.copy(pendingDeleteConnectionId = null) }

            ConnectionEvent.HideForm ->
                _uiState.update {
                    it.copy(
                        isFormVisible = false,
                        editingConnectionId = null,
                        form = ConnectionFormState(),
                        testResult = null
                    )
                }

            ConnectionEvent.ClearInfo ->
                _uiState.update { it.copy(infoMessage = null) }

            ConnectionEvent.ClearError ->
                _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun refreshConnections(keepEditingId: String? = _uiState.value.editingConnectionId) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getConnectionsUseCase().fold(
                onSuccess = { list ->
                    val connections = list.sortedWith(
                        compareByDescending<ApiConnection> { it.isActive }
                            .thenBy { it.name.lowercase() }
                    )

                    _uiState.update { current ->
                        val editingStillExists = keepEditingId != null &&
                            connections.any { it.id == keepEditingId }

                        current.copy(
                            isLoading = false,
                            connections = connections,
                            editingConnectionId = if (editingStillExists) keepEditingId else null,
                            form = if (editingStillExists || !current.isEditing) {
                                current.form
                            } else {
                                ConnectionFormState()
                            },
                            testResult = if (editingStillExists) current.testResult else null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = mapError(error)
                        )
                    }
                }
            )
        }
    }

    private fun startCreateMode() {
        _uiState.update {
            it.copy(
                editingConnectionId = null,
                form = ConnectionFormState(),
                testResult = null,
                errorMessage = null,
                isFormVisible = true
            )
        }
    }

    private fun startCreateWithPreset(serviceType: ServiceType) {
        val presetUrl = serviceType.defaultBaseUrl.orEmpty()
        val sanitized = sanitizeBaseUrl(presetUrl)
        _uiState.update {
            it.copy(
                editingConnectionId = null,
                form = ConnectionFormState(
                    name = serviceType.displayName,
                    serviceType = serviceType,
                    baseUrl = presetUrl,
                    resolvedBaseUrl = sanitized,
                    modelsEndpoint = if (sanitized.isNotBlank()) "$sanitized/v1/models" else "",
                    chatEndpoint = if (sanitized.isNotBlank()) "$sanitized/v1/chat/completions" else ""
                ),
                testResult = null,
                errorMessage = null,
                isFormVisible = true
            )
        }
    }

    private fun startEditMode(connectionId: String) {
        val connection = _uiState.value.connections.firstOrNull { it.id == connectionId }
        if (connection == null) {
            _uiState.update { it.copy(errorMessage = "连接不存在或已被删除") }
            return
        }

        _uiState.update {
            it.copy(
                editingConnectionId = connectionId,
                form = connection.toFormState(),
                testResult = null,
                errorMessage = null,
                isFormVisible = true
            )
        }

        viewModelScope.launch {
            getConnectionSecretUseCase(connectionId)
                .onSuccess { savedKey ->
                    _uiState.update { current ->
                        if (current.editingConnectionId != connectionId) return@update current
                        current.copy(
                            form = current.form.copy(
                                apiKey = "",
                                hasExistingApiKey = !savedKey.isNullOrBlank(),
                                existingApiKeyMask = maskApiKey(savedKey)
                            )
                        )
                    }
                }
                .onFailure { error ->
                    // 本地密钥读取失败（keyset 损坏等），静默降级而非弹错误
                    Log.w(TAG, "读取本地密钥失败，视为无已保存密钥", error)
                    _uiState.update { current ->
                        if (current.editingConnectionId != connectionId) return@update current
                        current.copy(
                            form = current.form.copy(
                                apiKey = "",
                                hasExistingApiKey = false,
                                existingApiKeyMask = ""
                            )
                        )
                    }
                }
        }
    }

    private fun saveConnection() {
        if (_uiState.value.isSaving) return

        val state = _uiState.value
        val draft = buildDraft(state.form).getOrElse { error ->
            _uiState.update { it.copy(errorMessage = mapError(error)) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    infoMessage = null
                )
            }

            val saveResult = if (state.editingConnectionId == null) {
                val shouldSetActive = state.connections.none { it.isActive }
                createConnectionUseCase(draft, shouldSetActive)
            } else {
                val origin = state.connections.firstOrNull { it.id == state.editingConnectionId }
                    ?: run {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = "连接已不存在，请刷新后重试"
                            )
                        }
                        return@launch
                    }
                updateConnectionUseCase(origin, draft)
            }

            saveResult.fold(
                onSuccess = { savedConnection ->
                    // 保存 API Key（仅当用户显式输入了新 Key 时）
                    val apiKeyToSave = state.form.apiKey
                    val shouldSaveKey = apiKeyToSave.isNotBlank()

                    val secretResult = if (shouldSaveKey) {
                        saveConnectionSecretUseCase(
                            connectionId = savedConnection.id,
                            apiKey = apiKeyToSave
                        )
                    } else {
                        Result.success(Unit)
                    }

                    val secretError = secretResult.exceptionOrNull()
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            editingConnectionId = savedConnection.id,
                            infoMessage = if (secretError != null) {
                                null
                            } else if (state.isEditing) {
                                "连接已更新"
                            } else {
                                "连接已创建"
                            },
                            errorMessage = secretError?.let {
                                val action = if (state.isEditing) "已更新" else "已创建"
                                "连接${action}，但密钥保存失败: ${mapError(it)}"
                            },
                            isFormVisible = false
                        )
                    }
                    refreshConnections(keepEditingId = savedConnection.id)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = mapError(error)
                        )
                    }
                }
            )
        }
    }

    private fun setAsDefault() {
        val connectionId = _uiState.value.editingConnectionId
        if (connectionId.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "请先选择要设为默认的连接") }
            return
        }
        if (_uiState.value.isSettingActive) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSettingActive = true, errorMessage = null) }

            setActiveConnectionUseCase(connectionId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSettingActive = false,
                            infoMessage = "已设为默认连接"
                        )
                    }
                    refreshConnections(keepEditingId = connectionId)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSettingActive = false,
                            errorMessage = mapError(error)
                        )
                    }
                }
        }
    }

    private fun testConnection() {
        val state = _uiState.value
        val connectionId = state.editingConnectionId
        if (connectionId.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "请先保存并选中连接后再测试") }
            return
        }
        val target = state.connections.firstOrNull { it.id == connectionId }
        if (target == null) {
            _uiState.update { it.copy(errorMessage = "连接不存在，请刷新后重试") }
            return
        }
        if (state.isTesting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isTesting = true, errorMessage = null, infoMessage = null) }

            testConnectionUseCase(
                connection = target,
                apiKeyOverride = state.form.apiKey.takeIf { it.isNotBlank() }
            ).fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            isTesting = false,
                            testResult = result,
                            infoMessage = if (result.success) {
                                "测试成功，耗时 ${result.elapsedMs} ms"
                            } else {
                                null
                            },
                            errorMessage = if (result.success) null else result.message
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isTesting = false,
                            testResult = null,
                            errorMessage = mapError(error)
                        )
                    }
                }
            )
        }
    }

    private fun fetchModels() {
        val form = _uiState.value.form
        if (form.isFetchingModels) return
        if (form.baseUrl.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请先填写 Base URL") }
            return
        }

        viewModelScope.launch {
            updateForm { copy(isFetchingModels = true) }

            val headersJson = parseJsonObject(
                form.headersTemplateJson, "headersTemplateJson"
            ).getOrElse {
                updateForm { copy(isFetchingModels = false) }
                _uiState.update { it.copy(errorMessage = "Headers 模板 JSON 格式错误") }
                return@launch
            }

            fetchModelsUseCase(
                baseUrl = sanitizeBaseUrl(form.baseUrl),
                apiKey = form.apiKey,
                connectionId = _uiState.value.editingConnectionId,
                headersTemplateJson = headersJson
            ).fold(
                onSuccess = { models ->
                    updateForm {
                        copy(
                            availableModels = models,
                            isFetchingModels = false
                        )
                    }
                    _uiState.update {
                        it.copy(infoMessage = "已拉取 ${models.size} 个模型")
                    }
                },
                onFailure = { error ->
                    updateForm { copy(isFetchingModels = false) }
                    _uiState.update { it.copy(errorMessage = mapError(error)) }
                }
            )
        }
    }

    private fun confirmDelete() {
        val state = _uiState.value
        val connectionId = state.pendingDeleteConnectionId ?: return
        if (state.isDeleting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }

            deleteConnectionUseCase(connectionId).fold(
                onSuccess = {
                    val wasEditing = state.editingConnectionId == connectionId
                    val keepEditingId = if (wasEditing) null else state.editingConnectionId

                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            pendingDeleteConnectionId = null,
                            editingConnectionId = keepEditingId,
                            form = if (wasEditing) ConnectionFormState() else it.form,
                            testResult = if (wasEditing) null else it.testResult,
                            infoMessage = "连接已删除",
                            isFormVisible = if (wasEditing) false else it.isFormVisible
                        )
                    }
                    refreshConnections(keepEditingId = keepEditingId)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            pendingDeleteConnectionId = null,
                            errorMessage = mapError(error)
                        )
                    }
                }
            )
        }
    }

    // ── 表单辅助方法 ──

    private fun updateForm(transform: ConnectionFormState.() -> ConnectionFormState) {
        _uiState.update { current ->
            current.copy(
                form = current.form.transform(),
                errorMessage = null,
                infoMessage = null
            )
        }
    }

    private fun buildDraft(form: ConnectionFormState): Result<ConnectionDraft> = runCatching {
        val headersTemplateJson = parseJsonObject(
            form.headersTemplateJson,
            "headersTemplateJson"
        ).getOrThrow()

        ConnectionDraft(
            name = form.name,
            serviceType = form.serviceType.serialName,
            baseUrl = sanitizeBaseUrl(form.baseUrl),
            defaultModel = form.defaultModel,
            headersTemplateJson = headersTemplateJson,
        )
    }

    private fun parseJsonObject(rawValue: String, fieldName: String): Result<JsonObject> = runCatching {
        val source = rawValue.trim().ifBlank { "{}" }
        val element = json.parseToJsonElement(source)
        element as? JsonObject
            ?: throw IllegalArgumentException("$fieldName 必须是 JSON 对象")
    }

    // ── 双向转换 ──

    private fun ApiConnection.toFormState(): ConnectionFormState {
        val sanitized = sanitizeBaseUrl(baseUrl)
        return ConnectionFormState(
            name = name,
            serviceType = ServiceType.fromSerialName(serviceType),
            baseUrl = baseUrl,
            defaultModel = defaultModel.orEmpty(),
            headersTemplateJson = headersTemplateJson.toPrettyString(),
            resolvedBaseUrl = sanitized,
            modelsEndpoint = if (sanitized.isNotBlank()) "$sanitized/v1/models" else "",
            chatEndpoint = if (sanitized.isNotBlank()) "$sanitized/v1/chat/completions" else "",
            apiKey = "",
            hasExistingApiKey = false,
            existingApiKeyMask = ""
        )
    }

    private fun JsonObject.toPrettyString(): String {
        if (isEmpty()) return "{}"
        return json.encodeToString(JsonObject.serializer(), this)
    }

    // ── 实时校验 ──

    private fun validateBaseUrlInstant(url: String): String? {
        if (url.isBlank()) return null // 空值在保存时校验
        val trimmed = url.trim()
        val uri = runCatching { URI(trimmed) }.getOrNull() ?: return "URL 格式不合法"
        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") return "URL 必须以 http:// 或 https:// 开头"
        if (uri.host.isNullOrBlank()) return "URL 缺少主机名"
        return null
    }

    private fun validateJsonInstant(rawValue: String, fieldName: String): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isBlank() || trimmed == "{}") return null
        return parseJsonObject(trimmed, fieldName).exceptionOrNull()?.message
    }

    private fun validateJsonAndHeaders(rawValue: String): String? {
        val jsonError = validateJsonInstant(rawValue, "headersTemplateJson")
        if (jsonError != null) return jsonError

        val trimmed = rawValue.trim()
        if (trimmed.isBlank() || trimmed == "{}") return null

        val parsed = parseJsonObject(trimmed, "headersTemplateJson").getOrNull() ?: return null
        val forbiddenHeaders = setOf("authorization", "x-api-key", "api-key")
        parsed.keys.forEach { key ->
            val normalized = key.trim().lowercase().replace("_", "-")
            if (normalized in forbiddenHeaders) {
                return "禁止包含密钥头「$key」，密钥请使用 API Key 字段"
            }
        }
        return null
    }

    // ── 辅助工具 ──

    /** 根据 Base URL 域名自动建议连接名称 */
    private fun suggestName(baseUrl: String): String {
        if (baseUrl.isBlank()) return ""
        val host = runCatching { URI(baseUrl.trim()).host }.getOrNull() ?: return ""
        return when {
            "openai" in host -> "OpenAI"
            "anthropic" in host -> "Claude"
            "googleapis" in host || "google" in host -> "Gemini"
            "deepseek" in host -> "DeepSeek"
            "moonshot" in host || "kimi" in host -> "Moonshot"
            else -> host.removePrefix("api.").removeSuffix(".com").removeSuffix(".ai")
                .replaceFirstChar { it.uppercase() }
        }
    }

    /** 判断当前 baseUrl 是否为某个 ServiceType 的默认 URL */
    private fun isDefaultBaseUrl(url: String): Boolean {
        val trimmed = url.trim().trimEnd('/')
        return ServiceType.entries.any { it.defaultBaseUrl?.trimEnd('/') == trimmed }
    }

    /** API Key 掩码：显示 sk-...xxxx */
    private fun maskApiKey(key: String?): String {
        if (key.isNullOrBlank()) return ""
        if (key.length <= 8) return "****"
        return "${key.take(3)}...${key.takeLast(4)}"
    }

    /**
     * 自动清理和格式化用户输入的 Base URL。
     * - 补充 https:// (如果没有)
     * - 移除常见的误复制路径如 /chat/completions 或 /v1/chat/completions
     * - 移除末尾斜杠
     */
    private fun sanitizeBaseUrl(url: String): String {
        var cleanUrl = url.trim()
        if (cleanUrl.isBlank()) return ""

        if (!cleanUrl.startsWith("http://", ignoreCase = true) && 
            !cleanUrl.startsWith("https://", ignoreCase = true)) {
            cleanUrl = "https://$cleanUrl"
        }

        // 移除用户常见的误输入后缀
        val suffixesToRemove = listOf(
            "/v1/chat/completions",
            "/chat/completions",
            "/v1/models",
            "/models",
            "/v1"
        )
        
        for (suffix in suffixesToRemove) {
            if (cleanUrl.endsWith(suffix, ignoreCase = true)) {
                cleanUrl = cleanUrl.dropLast(suffix.length)
                break 
            }
        }
        
        return cleanUrl.trimEnd('/')
    }

    private fun mapError(error: Throwable): String {
        val message = error.message.orEmpty()
        val lowered = message.lowercase()
        return when {
            "未登录" in message || "not authenticated" in lowered -> "请先登录后再操作"
            "base url" in lowered || "base_url" in lowered -> "Base URL 必须是合法的 http/https 地址"
            "headers_template_json" in lowered -> "Headers 模板包含受限密钥头"
            "duplicate key" in lowered || "user_name_unique" in lowered -> "连接名称已存在，请更换名称"
            "json" in lowered -> "JSON 格式错误，请检查 params 或 headers 模板"
            message.isNotBlank() -> message
            else -> "操作失败，请稍后重试"
        }
    }

    private companion object {
        const val TAG = "ConnectionVM"
    }
}
