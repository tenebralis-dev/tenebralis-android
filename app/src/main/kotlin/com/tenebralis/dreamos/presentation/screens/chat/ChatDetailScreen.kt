package com.tenebralis.dreamos.presentation.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tenebralis.dreamos.presentation.theme.*
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

    // 设置摘要作为 TopAppBar 副标题
    val settingSummary = buildSettingSummary(
        presetName = uiState.currentPresetName,
        connectionName = uiState.currentConnectionName
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.npcName ?: "对话",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (settingSummary.isNotEmpty()) {
                            Text(
                                text = settingSummary,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(ChatDetailEvent.ShowSettings) }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "设置"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
        bottomBar = {
            // ── Telegram 风格输入栏 ──
            Column {
                // AI 错误提示
                uiState.aiErrorMessage?.let { aiError ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = aiError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        TextButton(
                            onClick = { viewModel.onEvent(ChatDetailEvent.RetryAiCall) }
                        ) {
                            Text("重试", style = MaterialTheme.typography.labelMedium)
                        }
                        TextButton(
                            onClick = { viewModel.onEvent(ChatDetailEvent.ClearAiError) }
                        ) {
                            Text("忽略", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                // 重试提示
                if (!uiState.failedContent.isNullOrBlank()) {
                    TextButton(
                        enabled = !uiState.isSending && !uiState.isAiResponding,
                        onClick = { viewModel.onEvent(ChatDetailEvent.RetrySend) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("重试上次发送失败的消息", style = MaterialTheme.typography.labelMedium)
                    }
                }

                // AI 回复中提示
                AnimatedVisibility(
                    visible = uiState.isAiResponding && uiState.streamingContent == null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.5.dp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "正在输入...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // 输入栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // 输入框
                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = { value -> viewModel.onEvent(ChatDetailEvent.InputChanged(value)) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = { Text("输入消息...") },
                        maxLines = 4,
                        enabled = !uiState.isSending && !uiState.isAiResponding,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = { viewModel.onEvent(ChatDetailEvent.Send) }
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )

                    // 发送/停止按钮
                    if (uiState.isAiResponding) {
                        IconButton(
                            onClick = { viewModel.onEvent(ChatDetailEvent.StopStreaming) },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.error,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    } else {
                        IconButton(
                            enabled = !uiState.isSending && uiState.inputText.isNotBlank(),
                            onClick = { viewModel.onEvent(ChatDetailEvent.Send) },
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = if (uiState.inputText.isNotBlank())
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                )
                        ) {
                            if (uiState.isSending) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Send,
                                    contentDescription = "发送",
                                    tint = if (uiState.inputText.isNotBlank())
                                        Color.White
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        // ── 消息列表区域 ──
        if (uiState.isLoading && uiState.messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
            }
        } else if (uiState.emptyState) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "开始对话吧",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "输入消息并发送，AI 将会回复你",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
            val maxBubbleWidth = screenWidthDp * 0.85f
            val isDark = isSystemInDarkTheme()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 8.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = uiState.messages,
                    key = { it.id }
                ) { message ->
                    ChatBubble(
                        message = message,
                        maxWidth = maxBubbleWidth,
                        isDark = isDark
                    )
                }

                // 流式 assistant 气泡
                uiState.streamingContent?.let { streamingText ->
                    item(key = "streaming_bubble") {
                        StreamingChatBubble(
                            text = streamingText,
                            maxWidth = maxBubbleWidth,
                            isDark = isDark
                        )
                    }
                }
            }
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

// ─── IM 聊天气泡 ──────────────────────────────────────────

@Composable
private fun ChatBubble(
    message: ConversationMessage,
    maxWidth: androidx.compose.ui.unit.Dp,
    isDark: Boolean
) {
    val isUser = message.role == MessageRole.USER
    val isSystem = message.role == MessageRole.SYSTEM || message.role == MessageRole.TOOL

    val bubbleColor = when {
        isUser -> if (isDark) ChatBubbleUserDark else ChatBubbleUser
        isSystem -> if (isDark) ChatBubbleSystemDark else ChatBubbleSystem
        else -> if (isDark) ChatBubbleAiDark else ChatBubbleAi
    }

    val bubbleShape = when {
        isUser -> RoundedCornerShape(
            topStart = 18.dp, topEnd = 4.dp,
            bottomStart = 18.dp, bottomEnd = 18.dp
        )
        else -> RoundedCornerShape(
            topStart = 4.dp, topEnd = 18.dp,
            bottomStart = 18.dp, bottomEnd = 18.dp
        )
    }

    // 时间戳 HH:mm
    val timestamp = message.createdAt
        ?.takeIf { it.length >= 16 }
        ?.substring(11, 16)
        ?: ""

    // 系统消息居中显示
    if (isSystem) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .background(
                        color = bubbleColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .background(color = bubbleColor, shape = bubbleShape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (timestamp.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun StreamingChatBubble(
    text: String,
    maxWidth: androidx.compose.ui.unit.Dp,
    isDark: Boolean
) {
    val bubbleColor = if (isDark) ChatBubbleAiDark else ChatBubbleAi
    val bubbleShape = RoundedCornerShape(
        topStart = 4.dp, topEnd = 18.dp,
        bottomStart = 18.dp, bottomEnd = 18.dp
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .background(color = bubbleColor, shape = bubbleShape)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(10.dp),
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
        }
    }
}
