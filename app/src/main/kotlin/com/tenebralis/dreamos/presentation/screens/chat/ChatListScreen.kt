package com.tenebralis.dreamos.presentation.screens.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenebralis.dreamos.domain.model.AiPreset
import com.tenebralis.dreamos.domain.model.Conversation
import com.tenebralis.dreamos.domain.model.Npc

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onBackClick: () -> Unit,
    onNavigateToChatDetail: (conversationId: String) -> Unit,
    modifier: Modifier = Modifier,
    isRootTab: Boolean = false,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val npcNameById = remember(uiState.npcs) {
        uiState.npcs.associateBy({ it.id }, { it.name })
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(ChatListEvent.ClearError)
        }
    }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(ChatListEvent.ClearInfo)
        }
    }

    LaunchedEffect(uiState.navigateToConversationId) {
        uiState.navigateToConversationId?.let { conversationId ->
            onNavigateToChatDetail(conversationId)
            viewModel.onEvent(ChatListEvent.ConsumeNavigation)
        }
    }

    // 创建 NPC 弹窗
    if (uiState.showCreateNpcDialog) {
        CreateNpcDialog(
            isCreating = uiState.isCreatingNpc,
            onDismiss = { viewModel.onEvent(ChatListEvent.DismissCreateNpcDialog) },
            onConfirm = { name, description ->
                viewModel.onEvent(ChatListEvent.ConfirmCreateNpc(name, description))
            }
        )
    }

    // 预设选择底部弹窗
    if (uiState.showPresetPicker) {
        PresetPickerBottomSheet(
            presets = uiState.availablePresets,
            onDismiss = { viewModel.onEvent(ChatListEvent.DismissPresetPicker) },
            onSelect = { presetId ->
                viewModel.onEvent(ChatListEvent.ConfirmPresetSelection(presetId))
            }
        )
    }

    // 新建线程命名弹窗
    if (uiState.showNewThreadDialog) {
        NewThreadDialog(
            defaultName = uiState.newThreadDefaultName,
            onDismiss = { viewModel.onEvent(ChatListEvent.DismissNewThreadDialog) },
            onConfirm = { threadName ->
                viewModel.onEvent(ChatListEvent.ConfirmNewThread(threadName))
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isRootTab) "对话" else "对话列表") },
                navigationIcon = {
                    if (!isRootTab) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(ChatListEvent.Refresh) }) {
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
                onClick = { viewModel.onEvent(ChatListEvent.ShowCreateNpcDialog) }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "创建 NPC"
                )
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
        ) {
            if (uiState.isInitializing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
                }
                return@Column
            }

            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (uiState.emptyNpcState) {
                // 空状态：引导创建 NPC
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChatBubbleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "开始你的第一次对话",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "创建一个 NPC 角色来开始聊天",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        TextButton(onClick = { viewModel.onEvent(ChatListEvent.Refresh) }) {
                            Text("刷新")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // ── NPC 头像横排（类似 Telegram Stories） ──
                    if (uiState.npcs.isNotEmpty()) {
                        item(key = "npc_row") {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                // 创建新 NPC 入口
                                item(key = "add_npc") {
                                    NpcAvatarItem(
                                        name = "新建",
                                        initial = "+",
                                        isAdd = true,
                                        isLoading = false,
                                        onClick = { viewModel.onEvent(ChatListEvent.ShowCreateNpcDialog) }
                                    )
                                }
                                items(
                                    items = uiState.npcs,
                                    key = { "npc_${it.id}" }
                                ) { npc ->
                                    NpcAvatarItem(
                                        name = npc.name,
                                        initial = npc.name.take(1),
                                        isAdd = false,
                                        isLoading = uiState.isCreatingConversation && uiState.selectedNpcId == npc.id,
                                        onClick = { viewModel.onEvent(ChatListEvent.SelectNpc(npc.id)) },
                                        onLongClick = { viewModel.onEvent(ChatListEvent.ShowNewThreadDialog(npc.id)) }
                                    )
                                }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        }
                    }

                    // ── 会话列表（IM 风格行项） ──
                    if (uiState.emptyConversationState) {
                        item(key = "empty_conv") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "点击上方角色开始新对话",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        items(
                            items = uiState.conversations,
                            key = { it.id }
                        ) { conversation ->
                            ConversationRow(
                                conversation = conversation,
                                npcName = npcNameById[conversation.npcId],
                                onClick = {
                                    viewModel.onEvent(ChatListEvent.OpenConversation(conversation.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── 预设选择底部弹窗 ──────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PresetPickerBottomSheet(
    presets: List<AiPreset>,
    onDismiss: () -> Unit,
    onSelect: (presetId: String?) -> Unit
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
            Text(
                text = "选择预设",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 预设列表
            presets.forEach { preset ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(preset.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = preset.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        preset.source?.takeIf { it.isNotBlank() }?.let { source ->
                            Text(
                                text = "来源: $source",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // "不使用预设" 选项
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(null) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)
                )
            ) {
                Text(
                    text = "不使用预设",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                )
            }

            // 底部安全间距
            Spacer(modifier = Modifier.padding(bottom = 16.dp))
        }
    }
}

// ─── 新建线程弹窗 ──────────────────────────────────────

@Composable
private fun NewThreadDialog(
    defaultName: String,
    onDismiss: () -> Unit,
    onConfirm: (threadName: String) -> Unit
) {
    var threadName by rememberSaveable { mutableStateOf(defaultName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建对话线程") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "为新线程命名（将作为 thread_key）",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = threadName,
                    onValueChange = { threadName = it },
                    label = { Text("线程名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(threadName) },
                enabled = threadName.trim().isNotEmpty()
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

// ─── 创建 NPC 弹窗 ───────────────────────────────────────

@Composable
private fun CreateNpcDialog(
    isCreating: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isCreating) onDismiss() },
        title = { Text("创建 NPC") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称 *") },
                    singleLine = true,
                    enabled = !isCreating,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    maxLines = 3,
                    enabled = !isCreating,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, description) },
                enabled = name.trim().isNotEmpty() && !isCreating
            ) {
                if (isCreating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("创建")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isCreating
            ) {
                Text("取消")
            }
        }
    )
}

// ─── NPC 头像项（Telegram Stories 风格） ──────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NpcAvatarItem(
    name: String,
    initial: String,
    isAdd: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .combinedClickable(
                enabled = !isLoading,
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = if (isAdd)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = if (isAdd) FontWeight.Light else FontWeight.SemiBold,
                        color = if (isAdd)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─── 会话行项（Telegram 风格） ─────────────────────────────

@Composable
private fun ConversationRow(
    conversation: Conversation,
    npcName: String?,
    onClick: () -> Unit
) {
    val subtitle = conversation.summary?.takeIf { it.isNotBlank() } ?: "暂无消息"
    val displayNpcName = npcName?.takeIf { it.isNotBlank() } ?: "未知角色"
    val initial = displayNpcName.take(1)

    // 格式化时间戳：只取时间部分 HH:mm
    val timestamp = conversation.lastMessageAt
        ?.takeIf { it.length >= 16 }
        ?.substring(11, 16)
        ?: ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像圆圈
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 名称 + 最后消息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayNpcName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (timestamp.isNotEmpty()) {
                        Text(
                            text = timestamp,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(start = 76.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    }
}
