package com.tenebralis.dreamos.presentation.screens.preset

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenebralis.dreamos.domain.model.AiPreset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PresetListScreen(
    onBackClick: () -> Unit,
    onNavigateToEdit: (presetId: String) -> Unit,
    viewModel: PresetListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = getDisplayName(context.contentResolver, uri) ?: "unknown.json"
            viewModel.importFile(uri, fileName, context.contentResolver)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeError()
        }
    }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeInfo()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("预设管理") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "刷新"
                        )
                    }
                    IconButton(
                        onClick = {
                            filePickerLauncher.launch(arrayOf("application/json"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FileOpen,
                            contentDescription = "导入预设"
                        )
                    }
                    IconButton(onClick = viewModel::showCreateDialog) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "创建预设"
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
            if (uiState.isLoading || uiState.isImporting) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                    Text(
                        text = if (uiState.isImporting) "正在导入预设..." else "正在加载...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (uiState.emptyState) {
                EmptyPresetStateCard()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.presets,
                        key = { it.id }
                    ) { preset ->
                        PresetItemCard(
                            preset = preset,
                            onClick = { onNavigateToEdit(preset.id) },
                            onLongClick = { viewModel.showDeleteDialog(preset) }
                        )
                    }
                }
            }
        }
    }

    // ─── 创建弹窗 ────────────────────────────────────────

    if (uiState.isCreateDialogVisible) {
        AlertDialog(
            onDismissRequest = viewModel::dismissCreateDialog,
            title = { Text("创建预设") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.createName,
                        onValueChange = viewModel::updateCreateName,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("预设名称") },
                        singleLine = true
                    )
                    Text(
                        text = "创建后将使用默认的 Prompt 模板，你可以稍后编辑。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !uiState.isCreating,
                    onClick = viewModel::createPreset
                ) {
                    if (uiState.isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    }
                    Text("创建")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !uiState.isCreating,
                    onClick = viewModel::dismissCreateDialog
                ) {
                    Text("取消")
                }
            }
        )
    }

    // ─── 删除确认弹窗 ────────────────────────────────────

    if (uiState.isDeleteDialogVisible) {
        val targetName = uiState.deleteTarget?.name ?: ""
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            title = { Text("删除预设") },
            text = { Text("确定要删除「$targetName」吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    enabled = !uiState.isDeleting,
                    onClick = viewModel::confirmDelete
                ) {
                    if (uiState.isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    }
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !uiState.isDeleting,
                    onClick = viewModel::dismissDeleteDialog
                ) {
                    Text("取消")
                }
            }
        )
    }

    // ─── 导入冲突弹窗 ────────────────────────────────────

    if (uiState.isConflictDialogVisible) {
        val conflictName = uiState.conflictPresetName
        AlertDialog(
            onDismissRequest = viewModel::dismissConflictDialog,
            title = { Text("同名预设已存在") },
            text = {
                Text("名为「$conflictName」的预设已存在，请选择处理方式：")
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = viewModel::conflictOverwrite) {
                        Text("覆盖更新")
                    }
                    TextButton(onClick = viewModel::conflictRename) {
                        Text("重命名导入")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissConflictDialog) {
                    Text("取消")
                }
            }
        )
    }
}

// ─── 工具函数 ────────────────────────────────────────────

private fun getDisplayName(resolver: ContentResolver, uri: Uri): String? {
    return try {
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            } else null
        }
    } catch (_: Exception) {
        null
    }
}

// ─── 子组件 ──────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PresetItemCard(
    preset: AiPreset,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 图标
            Surface(
                modifier = Modifier.size(44.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Layers,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(28.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // 来源标签
                val sourceLabel = when (preset.source) {
                    "sillytavern_import" -> "来自 SillyTavern"
                    "manual" -> "手动创建"
                    else -> preset.source ?: "手动创建"
                }
                Text(
                    text = sourceLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyPresetStateCard() {
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
                text = "还没有预设",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "点击右上角 + 手动创建预设，或点击导入按钮从 SillyTavern 预设文件（JSON）导入。\n\n预设定义了 AI 上下文的组装方式（Prompt 排列顺序、启用状态等），兼容 SillyTavern 格式。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
