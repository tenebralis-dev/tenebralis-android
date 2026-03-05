package com.tenebralis.dreamos.presentation.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenebralis.dreamos.domain.model.AiPreset
import com.tenebralis.dreamos.domain.model.ApiConnection
import com.tenebralis.dreamos.domain.model.ConversationMessage
import com.tenebralis.dreamos.domain.model.enums.MessageRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    onBackClick: () -> Unit,
    viewModel: ChatDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    // ── 智能自动滚动 ──
    var isAtBottom by remember { mutableStateOf(true) }

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = layoutInfo.totalItemsCount
            totalItems <= 1 || lastVisible >= totalItems - 2
        }.collect { atBottom ->
            isAtBottom = atBottom
        }
    }

    val scrollTarget = uiState.messages.size + if (uiState.streamingContent != null) 1 else 0
    LaunchedEffect(scrollTarget, uiState.streamingContent) {
        if (isAtBottom) {
            val totalItems = uiState.messages.size + if (uiState.streamingContent != null) 1 else 0
            if (totalItems > 0) {
                listState.scrollToItem(totalItems - 1)
            }
        }
    }

    LaunchedEffect(uiState.isSending) {
        if (uiState.isSending) {
            isAtBottom = true
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(ChatDetailEvent.ClearError)
        }
    }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(ChatDetailEvent.ClearInfo)
        }
    }

    // 设置底部弹窗
    if (uiState.showSettings) {
        ChatSettingsBottomSheet(
            presets = uiState.availablePresets,
            connections = uiState.availableConnections,
            currentPresetId = uiState.currentPresetId,
            currentConnectionId = uiState.currentConnectionId,
            onDismiss = { viewModel.onEvent(ChatDetailEvent.DismissSettings) },
            onSelectPreset = { presetId ->
                viewModel.onEvent(ChatDetailEvent.ChangePreset(presetId))
            },
            onSelectConnection = { connectionId ->
                viewModel.onEvent(ChatDetailEvent.ChangeApiConnection(connectionId))
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("对话详情") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    // 设置按钮
                    IconButton(onClick = { viewModel.onEvent(ChatDetailEvent.ShowSettings) }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "设置"
                        )
                    }
                    IconButton(onClick = { viewModel.onEvent(ChatDetailEvent.Refresh) }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "刷新"
                        )
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 当前设置摘要条
            val settingSummary = buildSettingSummary(
                presetName = uiState.currentPresetName,
                connectionName = uiState.currentConnectionName
            )
            if (settingSummary.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { viewModel.onEvent(ChatDetailEvent.ShowSettings) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = settingSummary,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (uiState.isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                    Text(
                        text = "正在加载消息...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (uiState.emptyState) {
                EmptyMessageCard()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.messages,
                        key = { it.id }
                    ) { message ->
                        MessageItemCard(message = message)
                    }

                    // 流式 assistant 气泡
                    uiState.streamingContent?.let { streamingText ->
                        item(key = "streaming_bubble") {
                            StreamingMessageBubble(text = streamingText)
                        }
                    }
                }
            }

            // AI 正在回复指示器
            AnimatedVisibility(
                visible = uiState.isAiResponding && uiState.streamingContent == null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                    Text(
                        text = "AI 正在回复...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // AI 错误提示 + 重试按钮
            uiState.aiErrorMessage?.let { aiError ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = aiError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { viewModel.onEvent(ChatDetailEvent.RetryAiCall) }
                        ) {
                            Text("重试")
                        }
                        TextButton(
                            onClick = { viewModel.onEvent(ChatDetailEvent.ClearAiError) }
                        ) {
                            Text("忽略")
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            if (!uiState.failedContent.isNullOrBlank()) {
                TextButton(
                    enabled = !uiState.isSending && !uiState.isAiResponding,
                    onClick = { viewModel.onEvent(ChatDetailEvent.RetrySend) }
                ) {
                    Text("重试上次发送失败的消息")
                }
            }

            OutlinedTextField(
                value = uiState.inputText,
                onValueChange = { value -> viewModel.onEvent(ChatDetailEvent.InputChanged(value)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("输入消息") },
                maxLines = 4,
                enabled = !uiState.isSending && !uiState.isAiResponding,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = { viewModel.onEvent(ChatDetailEvent.Send) }
                ),
                trailingIcon = {
                    if (uiState.isAiResponding) {
                        IconButton(
                            onClick = { viewModel.onEvent(ChatDetailEvent.StopStreaming) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.error,
                                        shape = RoundedCornerShape(3.dp)
                                    )
                            )
                        }
                    } else {
                        IconButton(
                            enabled = !uiState.isSending,
                            onClick = { viewModel.onEvent(ChatDetailEvent.Send) }
                        ) {
                            if (uiState.isSending) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Send,
                                    contentDescription = "发送"
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

// ─── 设置摘要 ────────────────────────────────────────────

private fun buildSettingSummary(presetName: String?, connectionName: String?): String {
    val parts = mutableListOf<String>()
    if (connectionName != null) parts.add("API: $connectionName")
    if (presetName != null) parts.add("预设: $presetName")
    return parts.joinToString("  ·  ")
}

// ─── 设置底部弹窗 ────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatSettingsBottomSheet(
    presets: List<AiPreset>,
    connections: List<ApiConnection>,
    currentPresetId: String?,
    currentConnectionId: String?,
    onDismiss: () -> Unit,
    onSelectPreset: (presetId: String?) -> Unit,
    onSelectConnection: (connectionId: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // ── API 连接 ──
            Text(
                text = "API 连接",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // "使用默认连接" 选项
            SettingsOptionCard(
                label = "使用默认连接",
                sublabel = "使用全局 active 连接",
                isSelected = currentConnectionId == null,
                onClick = { onSelectConnection(null) }
            )

            connections.forEach { connection ->
                SettingsOptionCard(
                    label = connection.name,
                    sublabel = "${connection.serviceType} · ${connection.baseUrl}",
                    isSelected = connection.id == currentConnectionId,
                    onClick = { onSelectConnection(connection.id) }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // ── 预设 ──
            Text(
                text = "预设",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // "不使用预设" 选项
            SettingsOptionCard(
                label = "不使用预设",
                sublabel = "使用默认采样参数",
                isSelected = currentPresetId == null,
                onClick = { onSelectPreset(null) }
            )

            presets.forEach { preset ->
                SettingsOptionCard(
                    label = preset.name,
                    sublabel = preset.source?.takeIf { it.isNotBlank() }?.let { "来源: $it" },
                    isSelected = preset.id == currentPresetId,
                    onClick = { onSelectPreset(preset.id) }
                )
            }

            // 底部安全间距
            Spacer(modifier = Modifier.padding(bottom = 16.dp))
        }
    }
}

@Composable
private fun SettingsOptionCard(
    label: String,
    sublabel: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                )
                sublabel?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── 消息子组件 ──────────────────────────────────────────

@Composable
private fun MessageItemCard(message: ConversationMessage) {
    val isUser = message.role == MessageRole.USER
    val containerColor = when (message.role) {
        MessageRole.USER -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        MessageRole.ASSISTANT -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
        MessageRole.SYSTEM -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
        MessageRole.TOOL -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.88f),
            colors = CardDefaults.cardColors(containerColor = containerColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = roleLabel(message.role),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "seq ${message.seq}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium
                )
                message.createdAt?.takeIf { it.isNotBlank() }?.let { createdAt ->
                    Text(
                        text = createdAt.take(19),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StreamingMessageBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.88f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "助手",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "输入中",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun EmptyMessageCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "暂无消息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "输入消息并发送后，AI 将会回复你。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun roleLabel(role: MessageRole): String {
    return when (role) {
        MessageRole.USER -> "用户"
        MessageRole.ASSISTANT -> "助手"
        MessageRole.SYSTEM -> "系统"
        MessageRole.TOOL -> "工具"
    }
}
