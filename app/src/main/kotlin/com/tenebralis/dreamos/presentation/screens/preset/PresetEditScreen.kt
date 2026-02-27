package com.tenebralis.dreamos.presentation.screens.preset

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetEditScreen(
    onBackClick: () -> Unit,
    viewModel: PresetEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
                title = {
                    Text(
                        text = if (uiState.isLoading) "加载中..." else "编辑预设",
                        maxLines = 1
                    )
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
                    if (!uiState.isLoading && uiState.preset != null) {
                        IconButton(
                            onClick = viewModel::save,
                            enabled = uiState.hasUnsavedChanges && !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Save,
                                    contentDescription = "保存",
                                    tint = if (uiState.hasUnsavedChanges) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    }
                                )
                            }
                        }
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
        // Loading 状态
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.padding(8.dp))
                Text(
                    text = "正在加载预设...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        // 异常状态
        val preset = uiState.preset
        if (preset == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "预设不存在或加载失败",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
            return@Scaffold
        }

        // ─── 正式编辑内容 ──────────────────────────────────

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 名称
            OutlinedTextField(
                value = uiState.editedName,
                onValueChange = viewModel::updateName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("预设名称") },
                singleLine = true,
                supportingText = if (uiState.hasUnsavedChanges) {
                    { Text("有未保存的更改", color = MaterialTheme.colorScheme.tertiary) }
                } else null
            )

            // 📊 采样参数
            SamplingParamsSection(
                params = uiState.samplingParams,
                expanded = "sampling" in uiState.expandedSections,
                onToggle = { viewModel.toggleSection("sampling") },
                onUpdate = viewModel::updateSamplingParams
            )

            // ⚙️ 高级设置
            AdvancedSettingsSection(
                settings = uiState.advancedSettings,
                expanded = "advanced" in uiState.expandedSections,
                onToggle = { viewModel.toggleSection("advanced") },
                onUpdate = viewModel::updateAdvancedSettings
            )

            // 📝 辅助提示词
            UtilityPromptsSection(
                prompts = uiState.utilityPrompts,
                expanded = "utility" in uiState.expandedSections,
                onToggle = { viewModel.toggleSection("utility") },
                onUpdate = viewModel::updateUtilityPrompts
            )

            // 📋 提示词列表
            PromptListSection(
                prompts = uiState.prompts,
                expanded = "prompts" in uiState.expandedSections,
                onToggle = { viewModel.toggleSection("prompts") },
                onEditPrompt = viewModel::openPromptEditor,
                onToggleEnabled = viewModel::togglePromptEnabled,
                onInsertBelow = viewModel::insertPromptBelow,
                onAddPrompt = viewModel::addPrompt
            )

            // 🔢 提示词排列顺序
            PromptOrderSection(
                groups = uiState.promptOrders,
                expanded = "order" in uiState.expandedSections,
                onToggle = { viewModel.toggleSection("order") },
                onMoveUp = viewModel::movePromptOrderUp,
                onMoveDown = viewModel::movePromptOrderDown,
                onToggleEnabled = viewModel::togglePromptOrderEnabled
            )

            // 🔗 连接/模型信息（只读）
            ConnectionInfoSection(
                presetJson = preset.presetJson,
                expanded = "connection" in uiState.expandedSections,
                onToggle = { viewModel.toggleSection("connection") }
            )

            Spacer(modifier = Modifier.padding(bottom = 32.dp))
        }
    }

    // ─── Prompt 编辑弹窗 ──────────────────────────────────

    val editingPrompt = uiState.editingPrompt
    if (editingPrompt != null) {
        PromptEditDialog(
            prompt = editingPrompt,
            onDismiss = viewModel::closePromptEditor,
            onConfirm = viewModel::confirmPromptEdit,
            onDelete = if (!editingPrompt.marker && !editingPrompt.systemPrompt) {
                { viewModel.deletePrompt(uiState.editingPromptIndex) }
            } else null
        )
    }
}
