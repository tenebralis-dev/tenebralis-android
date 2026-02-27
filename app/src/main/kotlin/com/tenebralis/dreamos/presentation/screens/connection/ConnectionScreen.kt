package com.tenebralis.dreamos.presentation.screens.connection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenebralis.dreamos.domain.model.ServiceType
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(
    onBackClick: () -> Unit,
    viewModel: ConnectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(ConnectionEvent.ClearError)
        }
    }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(ConnectionEvent.ClearInfo)
        }
    }

    val deletingConnection = uiState.connections
        .firstOrNull { it.id == uiState.pendingDeleteConnectionId }
    val deletingConnectionName = deletingConnection?.name.orEmpty()
    val isDeletingActiveConnection = deletingConnection?.isActive == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("连接") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(ConnectionEvent.Refresh) }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "刷新"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(ConnectionEvent.StartCreate) }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "新建连接")
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "加载连接中...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            ConnectionListSection(
                state = uiState,
                onEvent = viewModel::onEvent,
                onEdit = { id -> viewModel.onEvent(ConnectionEvent.EditConnection(id)) },
                onSetDefault = { id ->
                    viewModel.onEvent(ConnectionEvent.EditConnection(id))
                    viewModel.onEvent(ConnectionEvent.SetAsDefault)
                },
                onDelete = { id -> viewModel.onEvent(ConnectionEvent.RequestDelete(id)) }
            )
        }
    }

    // ── BottomSheet 编辑/新建表单 ──
    if (uiState.isFormVisible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(ConnectionEvent.HideForm) },
            sheetState = sheetState
        ) {
            ConnectionFormContent(
                state = uiState,
                onEvent = viewModel::onEvent
            )
        }
    }

    // ── 删除确认对话框 ──
    if (uiState.pendingDeleteConnectionId != null) {
        val deleteMessage = buildString {
            append("确认删除连接「${deletingConnectionName.ifBlank { "未命名连接" }}」吗？")
            if (isDeletingActiveConnection) {
                append("\n\n⚠️ 这是当前默认连接，删除后需重新选择一个连接才能使用 AI 功能。")
            }
            append("\n\n已保存的本地 API Key 也会被清除。")
        }

        AlertDialog(
            onDismissRequest = {
                if (!uiState.isDeleting) {
                    viewModel.onEvent(ConnectionEvent.DismissDeleteDialog)
                }
            },
            title = { Text("删除连接") },
            text = { Text(deleteMessage) },
            confirmButton = {
                TextButton(
                    enabled = !uiState.isDeleting,
                    onClick = { viewModel.onEvent(ConnectionEvent.ConfirmDelete) }
                ) {
                    if (uiState.isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !uiState.isDeleting,
                    onClick = { viewModel.onEvent(ConnectionEvent.DismissDeleteDialog) }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════════
//  连接列表
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ConnectionListSection(
    state: ConnectionUiState,
    onEvent: (ConnectionEvent) -> Unit,
    onEdit: (String) -> Unit,
    onSetDefault: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (state.connections.isEmpty() && !state.isLoading) {
        EmptyConnectionGuide(onEvent = onEvent)
        return
    }

    state.connections.forEachIndexed { index, connection ->
        ConnectionListItem(
            state = state,
            connectionId = connection.id,
            name = connection.name,
            serviceType = ServiceType.fromSerialName(connection.serviceType).displayName,
            baseUrl = connection.baseUrl,
            model = connection.defaultModel,
            isActive = connection.isActive,
            onEdit = { onEdit(connection.id) },
            onSetDefault = { onSetDefault(connection.id) },
            onDelete = { onDelete(connection.id) }
        )
        if (index < state.connections.lastIndex) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun EmptyConnectionGuide(onEvent: (ConnectionEvent) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "还没有连接配置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "连接是与 AI 服务通信的桥梁。选择一个服务快速开始，或点击右下角 + 自定义创建。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 预设快速开始按钮
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ServiceType.entries.forEach { type ->
                    OutlinedButton(
                        onClick = { onEvent(ConnectionEvent.StartCreateWithPreset(type)) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = type.displayName +
                                (type.defaultBaseUrl?.let { " · $it" } ?: "")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionListItem(
    state: ConnectionUiState,
    connectionId: String,
    name: String,
    serviceType: String,
    baseUrl: String,
    model: String?,
    isActive: Boolean,
    onEdit: () -> Unit,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEdit)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Medium
                )
                if (isActive) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Active",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Text(
                text = serviceType,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = baseUrl,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!model.isNullOrBlank()) {
                Text(
                    text = "模型: $model",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Text(if (state.editingConnectionId == connectionId) "编辑中" else "编辑")
                }
                TextButton(
                    enabled = !state.isSettingActive && !isActive,
                    onClick = onSetDefault
                ) {
                    Text("设默认")
                }
                TextButton(
                    enabled = !state.isDeleting,
                    onClick = onDelete
                ) {
                    Text("删除")
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  BottomSheet 表单内容
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ConnectionFormContent(
    state: ConnectionUiState,
    onEvent: (ConnectionEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (state.isEditing) "编辑连接" else "新建连接",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // ── 📋 基本信息 ──
        FormSectionHeader(title = "📋 基本信息")
        BasicInfoSection(state = state, onEvent = onEvent)

        Spacer(modifier = Modifier.height(4.dp))

        // ── ⚙️ 高级配置（默认收起）──
        CollapsibleSection(title = "⚙️ 高级配置", defaultExpanded = false) {
            AdvancedConfigSection(state = state, onEvent = onEvent)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── 🔑 密钥 ──
        FormSectionHeader(title = "🔑 密钥")
        ApiKeySection(state = state, onEvent = onEvent)

        Spacer(modifier = Modifier.height(8.dp))

        // ── 操作按钮 ──
        ActionButtonsSection(state = state, onEvent = onEvent)

        // ── 测试结果 ──
        state.testResult?.let { result ->
            Spacer(modifier = Modifier.height(4.dp))
            ConnectionTestResultCard(
                success = result.success,
                statusCode = result.statusCode,
                elapsedMs = result.elapsedMs,
                message = result.message
            )
        }
    }
}

// ── 基本信息分组 ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BasicInfoSection(
    state: ConnectionUiState,
    onEvent: (ConnectionEvent) -> Unit
) {
    val form = state.form

    // 名称
    OutlinedTextField(
        value = form.name,
        onValueChange = { onEvent(ConnectionEvent.NameChanged(it)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("名称") },
        singleLine = true,
        isError = form.nameError != null,
        supportingText = form.nameError?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
    )

    // Service Type 下拉
    var serviceTypeExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = serviceTypeExpanded,
        onExpandedChange = { serviceTypeExpanded = it }
    ) {
        OutlinedTextField(
            value = form.serviceType.displayName,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            label = { Text("服务类型") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceTypeExpanded) },
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = serviceTypeExpanded,
            onDismissRequest = { serviceTypeExpanded = false }
        ) {
            ServiceType.entries.forEach { type ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(type.displayName)
                            type.defaultBaseUrl?.let { url ->
                                Text(
                                    text = url,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    onClick = {
                        onEvent(ConnectionEvent.ServiceTypeSelected(type))
                        serviceTypeExpanded = false
                    }
                )
            }
        }
    }

    // Base URL
    OutlinedTextField(
        value = form.baseUrl,
        onValueChange = { onEvent(ConnectionEvent.BaseUrlChanged(it)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Base URL") },
        singleLine = true,
        isError = form.baseUrlError != null,
        supportingText = form.baseUrlError?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Next
        )
    )

    // Default Model（可编辑下拉 + 拉取按钮）
    var modelDropdownExpanded by remember { mutableStateOf(false) }
    val filteredModels = remember(form.defaultModel, form.availableModels) {
        if (form.defaultModel.isBlank()) {
            form.availableModels
        } else {
            form.availableModels.filter {
                it.contains(form.defaultModel, ignoreCase = true)
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = modelDropdownExpanded && filteredModels.isNotEmpty(),
        onExpandedChange = {
            modelDropdownExpanded = it
        }
    ) {
        OutlinedTextField(
            value = form.defaultModel,
            onValueChange = {
                onEvent(ConnectionEvent.DefaultModelChanged(it))
                modelDropdownExpanded = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable),
            label = { Text("Default Model (可选)") },
            singleLine = true,
            trailingIcon = {
                if (form.availableModels.isNotEmpty()) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelDropdownExpanded)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        if (filteredModels.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = modelDropdownExpanded,
                onDismissRequest = { modelDropdownExpanded = false }
            ) {
                filteredModels.forEach { modelId ->
                    DropdownMenuItem(
                        text = { Text(modelId, style = MaterialTheme.typography.bodyMedium) },
                        onClick = {
                            onEvent(ConnectionEvent.DefaultModelChanged(modelId))
                            modelDropdownExpanded = false
                        }
                    )
                }
            }
        }
    }

    // 拉取模型按钮
    OutlinedButton(
        onClick = { onEvent(ConnectionEvent.FetchModels) },
        enabled = !form.isFetchingModels && form.baseUrl.isNotBlank(),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (form.isFetchingModels) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(if (form.availableModels.isEmpty()) "拉取模型列表" else "刷新模型列表 (${form.availableModels.size})")
    }
}

// ── 高级配置分组 ──

@Composable
private fun AdvancedConfigSection(
    state: ConnectionUiState,
    onEvent: (ConnectionEvent) -> Unit
) {
    val form = state.form

    // Headers Template JSON
    OutlinedTextField(
        value = form.headersTemplateJson,
        onValueChange = { onEvent(ConnectionEvent.HeadersTemplateJsonChanged(it)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Headers 模板 (JSON)") },
        isError = form.headersJsonError != null,
        supportingText = form.headersJsonError?.let { { Text(it) } },
        minLines = 2,
        maxLines = 6
    )
}

// ── 密钥分组 ──

@Composable
private fun ApiKeySection(
    state: ConnectionUiState,
    onEvent: (ConnectionEvent) -> Unit
) {
    val form = state.form
    var apiKeyVisible by rememberSaveable { mutableStateOf(false) }

    if (form.hasExistingApiKey && form.existingApiKeyMask.isNotBlank()) {
        Text(
            text = "当前 Key: ${form.existingApiKeyMask}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }

    OutlinedTextField(
        value = form.apiKey,
        onValueChange = { onEvent(ConnectionEvent.ApiKeyChanged(it)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("API Key（本地安全存储）") },
        placeholder = {
            Text(
                if (form.hasExistingApiKey) "留空保持当前 Key 不变" else "输入 API Key"
            )
        },
        singleLine = true,
        visualTransformation = if (apiKeyVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                Icon(
                    imageVector = if (apiKeyVisible) {
                        Icons.Filled.VisibilityOff
                    } else {
                        Icons.Filled.Visibility
                    },
                    contentDescription = if (apiKeyVisible) "隐藏 API Key" else "显示 API Key"
                )
            }
        }
    )
}

// ── 操作按钮 ──

@Composable
private fun ActionButtonsSection(
    state: ConnectionUiState,
    onEvent: (ConnectionEvent) -> Unit
) {
    Button(
        onClick = { onEvent(ConnectionEvent.Save) },
        enabled = !state.isSaving,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (state.isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text("保存")
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { onEvent(ConnectionEvent.SetAsDefault) },
            enabled = state.isEditing && !state.isSettingActive,
            modifier = Modifier.weight(1f)
        ) {
            if (state.isSettingActive) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("设为默认")
        }
        OutlinedButton(
            onClick = { onEvent(ConnectionEvent.TestConnection) },
            enabled = state.isEditing && !state.isTesting,
            modifier = Modifier.weight(1f)
        ) {
            if (state.isTesting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("测试连接")
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  通用组件
// ═══════════════════════════════════════════════════════════════

@Composable
private fun FormSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun CollapsibleSection(
    title: String,
    defaultExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(defaultExpanded) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                },
                contentDescription = if (expanded) "收起" else "展开",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun ConnectionTestResultCard(
    success: Boolean,
    statusCode: Int?,
    elapsedMs: Long,
    message: String
) {
    val containerColor = if (success) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    } else {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
    }
    val contentColor = if (success) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (success) "连接测试通过" else "连接测试失败",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
            Text(
                text = "耗时: ${elapsedMs}ms",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor
            )
            statusCode?.let {
                Text(
                    text = "HTTP 状态: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}
