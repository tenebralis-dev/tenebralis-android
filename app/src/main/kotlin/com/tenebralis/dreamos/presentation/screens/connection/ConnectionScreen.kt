package com.tenebralis.dreamos.presentation.screens.connection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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

    val deletingConnectionName = uiState.connections
        .firstOrNull { it.id == uiState.pendingDeleteConnectionId }
        ?.name
        .orEmpty()

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
                    TextButton(
                        onClick = { viewModel.onEvent(ConnectionEvent.StartCreate) }
                    ) {
                        Text("新建")
                    }
                }
            )
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
                onEdit = { id -> viewModel.onEvent(ConnectionEvent.EditConnection(id)) },
                onSetDefault = { id ->
                    viewModel.onEvent(ConnectionEvent.EditConnection(id))
                    viewModel.onEvent(ConnectionEvent.SetAsDefault)
                },
                onDelete = { id -> viewModel.onEvent(ConnectionEvent.RequestDelete(id)) }
            )

            ConnectionFormSection(
                state = uiState,
                onEvent = viewModel::onEvent
            )

            uiState.testResult?.let { result ->
                ConnectionTestResultCard(
                    success = result.success,
                    statusCode = result.statusCode,
                    elapsedMs = result.elapsedMs,
                    message = result.message
                )
            }
        }
    }

    if (uiState.pendingDeleteConnectionId != null) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isDeleting) {
                    viewModel.onEvent(ConnectionEvent.DismissDeleteDialog)
                }
            },
            title = { Text("删除连接") },
            text = {
                Text("确认删除连接「${deletingConnectionName.ifBlank { "未命名连接" }}」吗？已保存的本地 API Key 也会被清除。")
            },
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

@Composable
private fun ConnectionListSection(
    state: ConnectionUiState,
    onEdit: (String) -> Unit,
    onSetDefault: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "连接列表",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (state.connections.isEmpty()) {
                Text(
                    text = "暂无连接，先创建一个连接配置。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                state.connections.forEachIndexed { index, connection ->
                    ConnectionListItem(
                        state = state,
                        connectionId = connection.id,
                        name = connection.name,
                        serviceType = connection.serviceType,
                        baseUrl = connection.baseUrl,
                        model = connection.defaultModel,
                        isActive = connection.isActive,
                        onEdit = { onEdit(connection.id) },
                        onSetDefault = { onSetDefault(connection.id) },
                        onDelete = { onDelete(connection.id) }
                    )
                    if (index < state.connections.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
            text = "service: $serviceType",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "baseUrl: $baseUrl",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!model.isNullOrBlank()) {
            Text(
                text = "model: $model",
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

@Composable
private fun ConnectionFormSection(
    state: ConnectionUiState,
    onEvent: (ConnectionEvent) -> Unit
) {
    var apiKeyVisible by rememberSaveable { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (state.isEditing) "编辑连接" else "新建连接",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = state.form.name,
                onValueChange = { onEvent(ConnectionEvent.NameChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("名称") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = state.form.serviceType,
                onValueChange = { onEvent(ConnectionEvent.ServiceTypeChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Service Type") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = state.form.baseUrl,
                onValueChange = { onEvent(ConnectionEvent.BaseUrlChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Base URL") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = state.form.defaultModel,
                onValueChange = { onEvent(ConnectionEvent.DefaultModelChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Default Model (可选)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = state.form.systemPrompt,
                onValueChange = { onEvent(ConnectionEvent.SystemPromptChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("System Prompt (可选)") },
                minLines = 3,
                maxLines = 6
            )

            OutlinedTextField(
                value = state.form.paramsJson,
                onValueChange = { onEvent(ConnectionEvent.ParamsJsonChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("paramsJson") },
                minLines = 3,
                maxLines = 8
            )

            OutlinedTextField(
                value = state.form.headersTemplateJson,
                onValueChange = { onEvent(ConnectionEvent.HeadersTemplateJsonChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("headersTemplateJson") },
                minLines = 3,
                maxLines = 8
            )

            OutlinedTextField(
                value = state.form.apiKey,
                onValueChange = { onEvent(ConnectionEvent.ApiKeyChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("API Key（本地安全存储）") },
                placeholder = { Text("留空并保存会清除本地已保存 Key") },
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

            Spacer(modifier = Modifier.height(2.dp))

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
