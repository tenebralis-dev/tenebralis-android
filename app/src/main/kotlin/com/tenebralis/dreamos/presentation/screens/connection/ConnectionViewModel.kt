package com.tenebralis.dreamos.presentation.screens.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.model.ApiConnection
import com.tenebralis.dreamos.domain.usecase.connection.ConnectionDraft
import com.tenebralis.dreamos.domain.usecase.connection.CreateConnectionUseCase
import com.tenebralis.dreamos.domain.usecase.connection.DeleteConnectionUseCase
import com.tenebralis.dreamos.domain.usecase.connection.GetConnectionSecretUseCase
import com.tenebralis.dreamos.domain.usecase.connection.GetConnectionsUseCase
import com.tenebralis.dreamos.domain.usecase.connection.SaveConnectionSecretUseCase
import com.tenebralis.dreamos.domain.usecase.connection.SetActiveConnectionUseCase
import com.tenebralis.dreamos.domain.usecase.connection.TestConnectionUseCase
import com.tenebralis.dreamos.domain.usecase.connection.UpdateConnectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val testConnectionUseCase: TestConnectionUseCase
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
            is ConnectionEvent.EditConnection -> startEditMode(event.connectionId)

            is ConnectionEvent.NameChanged ->
                updateForm { copy(name = event.value) }

            is ConnectionEvent.ServiceTypeChanged ->
                updateForm { copy(serviceType = event.value) }

            is ConnectionEvent.BaseUrlChanged ->
                updateForm { copy(baseUrl = event.value) }

            is ConnectionEvent.DefaultModelChanged ->
                updateForm { copy(defaultModel = event.value) }

            is ConnectionEvent.SystemPromptChanged ->
                updateForm { copy(systemPrompt = event.value) }

            is ConnectionEvent.ParamsJsonChanged ->
                updateForm { copy(paramsJson = event.value) }

            is ConnectionEvent.HeadersTemplateJsonChanged ->
                updateForm { copy(headersTemplateJson = event.value) }

            is ConnectionEvent.ApiKeyChanged ->
                updateForm { copy(apiKey = event.value) }

            ConnectionEvent.Save -> saveConnection()
            ConnectionEvent.SetAsDefault -> setAsDefault()
            ConnectionEvent.TestConnection -> testConnection()

            is ConnectionEvent.RequestDelete ->
                _uiState.update { it.copy(pendingDeleteConnectionId = event.connectionId) }

            ConnectionEvent.ConfirmDelete -> confirmDelete()
            ConnectionEvent.DismissDeleteDialog ->
                _uiState.update { it.copy(pendingDeleteConnectionId = null) }

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
                errorMessage = null
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
                errorMessage = null
            )
        }

        viewModelScope.launch {
            getConnectionSecretUseCase(connectionId)
                .onSuccess { savedKey ->
                    _uiState.update { current ->
                        if (current.editingConnectionId != connectionId) return@update current
                        current.copy(
                            form = current.form.copy(apiKey = savedKey.orEmpty())
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = mapError(error)) }
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
                    val secretResult = saveConnectionSecretUseCase(
                        connectionId = savedConnection.id,
                        apiKey = state.form.apiKey
                    )

                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            editingConnectionId = savedConnection.id,
                            infoMessage = if (state.isEditing) {
                                "连接已更新"
                            } else {
                                "连接已创建"
                            },
                            errorMessage = secretResult.exceptionOrNull()?.let(::mapError)
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
                            infoMessage = "连接已删除"
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
        val paramsJson = parseJsonObject(form.paramsJson, "paramsJson").getOrThrow()
        val headersTemplateJson = parseJsonObject(
            form.headersTemplateJson,
            "headersTemplateJson"
        ).getOrThrow()

        ConnectionDraft(
            name = form.name,
            serviceType = form.serviceType,
            baseUrl = form.baseUrl,
            defaultModel = form.defaultModel,
            systemPrompt = form.systemPrompt,
            paramsJson = paramsJson,
            headersTemplateJson = headersTemplateJson
        )
    }

    private fun parseJsonObject(rawValue: String, fieldName: String): Result<JsonObject> = runCatching {
        val source = rawValue.trim().ifBlank { "{}" }
        val element = json.parseToJsonElement(source)
        element as? JsonObject
            ?: throw IllegalArgumentException("$fieldName 必须是 JSON 对象")
    }

    private fun ApiConnection.toFormState(): ConnectionFormState = ConnectionFormState(
        name = name,
        serviceType = serviceType,
        baseUrl = baseUrl,
        defaultModel = defaultModel.orEmpty(),
        systemPrompt = systemPrompt.orEmpty(),
        paramsJson = paramsJson.toPrettyString(),
        headersTemplateJson = headersTemplateJson.toPrettyString(),
        apiKey = ""
    )

    private fun JsonObject.toPrettyString(): String {
        if (isEmpty()) return "{}"
        return json.encodeToString(JsonObject.serializer(), this)
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
}
