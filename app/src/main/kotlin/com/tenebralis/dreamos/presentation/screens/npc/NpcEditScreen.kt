package com.tenebralis.dreamos.presentation.screens.npc

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NpcEditScreen(
    onBackClick: () -> Unit,
    viewModel: NpcEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // ─── 裁剪结果接收器 ──────────────────────────────────
    val cropLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { croppedUri ->
                viewModel.onImagePicked(croppedUri)
            }
        }
        // 用户取消裁剪则不做任何操作
    }

    // ─── 图片选择器 → 判断 GIF 跳过裁剪，否则进入裁剪 ──
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult

        val mime = context.contentResolver.getType(uri)
        if (mime == "image/gif") {
            // GIF 动图：跳过裁剪，直接上传
            viewModel.onImagePicked(uri)
        } else {
            // 静态图片：启动正方形裁剪
            cropLauncher.launch(
                CropImageContractOptions(
                    uri = uri,
                    cropImageOptions = CropImageOptions(
                        fixAspectRatio = true,
                        aspectRatioX = 1,
                        aspectRatioY = 1,
                        outputCompressFormat = Bitmap.CompressFormat.PNG,
                        outputCompressQuality = 100  // 裁剪后不压缩，后续由 ImageCompressor 统一压缩
                    )
                )
            )
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

    // 保存成功后返回
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) onBackClick()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑 NPC") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::save,
                        enabled = !uiState.isSaving && !uiState.isLoading && !uiState.isUploading
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Save,
                                contentDescription = "保存"
                            )
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ─── 头像区域 ─────────────────────────────
                AvatarSection(
                    avatarUrl = uiState.avatarUrl,
                    name = uiState.name,
                    isUploading = uiState.isUploading,
                    onClick = viewModel::showAvatarDialog
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ─── 基本信息 ─────────────────────────────
                SectionHeader("基本信息")

                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("名称") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::updateDescription,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("描述") },
                    minLines = 2,
                    maxLines = 6
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ─── 角色卡参数 ───────────────────────────
                SectionHeader("角色卡参数")

                OutlinedTextField(
                    value = uiState.firstMessage,
                    onValueChange = viewModel::updateFirstMessage,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("首条消息 (first_mes)") },
                    minLines = 2,
                    maxLines = 8
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.personality,
                    onValueChange = viewModel::updatePersonality,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("性格 (personality)") },
                    minLines = 2,
                    maxLines = 6
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.scenario,
                    onValueChange = viewModel::updateScenario,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("场景 (scenario)") },
                    minLines = 2,
                    maxLines = 6
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.mesExample,
                    onValueChange = viewModel::updateMesExample,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("对话示例 (mes_example)") },
                    minLines = 2,
                    maxLines = 8
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.systemPrompt,
                    onValueChange = viewModel::updateSystemPrompt,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("系统提示词 (system_prompt)") },
                    minLines = 2,
                    maxLines = 8
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.postHistoryInstructions,
                    onValueChange = viewModel::updatePostHistoryInstructions,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("历史后指令 (post_history_instructions)") },
                    minLines = 2,
                    maxLines = 6
                )

                // ─── 元信息（只读） ──────────────────────
                if (uiState.source.isNotBlank() || uiState.creator.isNotBlank() ||
                    uiState.characterVersion.isNotBlank() || uiState.creatorNotes.isNotBlank()
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    SectionHeader("元信息")

                    if (uiState.source.isNotBlank()) {
                        ReadOnlyField("来源", uiState.source)
                    }
                    if (uiState.creator.isNotBlank()) {
                        ReadOnlyField("创建者", uiState.creator)
                    }
                    if (uiState.characterVersion.isNotBlank()) {
                        ReadOnlyField("版本号", uiState.characterVersion)
                    }
                    if (uiState.tags.isNotEmpty()) {
                        ReadOnlyField("标签", uiState.tags.joinToString(", "))
                    }
                    if (uiState.creatorNotes.isNotBlank()) {
                        ReadOnlyField("创建者备注", uiState.creatorNotes)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // ─── 头像修改弹窗 ─────────────────────────────────────
    if (uiState.isAvatarDialogVisible) {
        AvatarEditDialog(
            urlInput = uiState.avatarDialogInput,
            onUrlInputChange = viewModel::updateAvatarDialogInput,
            onConfirmUrl = viewModel::confirmAvatarUrl,
            onPickImage = {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onDismiss = viewModel::dismissAvatarDialog
        )
    }
}

// ─── 子组件 ──────────────────────────────────────────────

@Composable
private fun AvatarSection(
    avatarUrl: String,
    name: String,
    isUploading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.clickable(enabled = !isUploading, onClick = onClick),
        contentAlignment = Alignment.BottomEnd
    ) {
        Surface(
            modifier = Modifier.size(96.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            if (isUploading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                }
            } else if (avatarUrl.isNotBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.large)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(20.dp)
                        .size(56.dp)
                )
            }
        }

        // 编辑图标角标
        if (!isUploading) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "修改头像",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

/**
 * 头像编辑弹窗：从相册选择（自动裁剪）+ 输入直链
 */
@Composable
private fun AvatarEditDialog(
    urlInput: String,
    onUrlInputChange: (String) -> Unit,
    onConfirmUrl: () -> Unit,
    onPickImage: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改头像") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 从相册选择按钮
                Button(
                    onClick = onPickImage,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("从相册选择")
                }

                Text(
                    text = "静态图片会自动弹出正方形裁剪，GIF 动图直接上传",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 分隔线 "或"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Text(
                        text = "或",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                // 直链输入框
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = onUrlInputChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("头像 URL") },
                    placeholder = { Text("https://...") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirmUrl) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun SectionHeader(title: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun ReadOnlyField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
