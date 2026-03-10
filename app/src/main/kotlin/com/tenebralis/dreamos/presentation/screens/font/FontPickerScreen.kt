package com.tenebralis.dreamos.presentation.screens.font

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenebralis.dreamos.domain.model.FontItem
import com.tenebralis.dreamos.domain.model.enums.FontSource
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontPickerScreen(
    onBack: () -> Unit,
    viewModel: FontPickerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFabMenu by remember { mutableStateOf(false) }

    // ── 文件选择器 ──
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "自定义字体"
            viewModel.onEvent(FontPickerEvent.ImportLocalFont(uri, name))
        }
    }

    // ── Snackbar 处理 ──
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(FontPickerEvent.DismissError)
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(FontPickerEvent.DismissSuccess)
        }
    }

    // ── 删除确认对话框 ──
    uiState.showDeleteDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(FontPickerEvent.DismissDeleteDialog) },
            title = { Text("删除字体") },
            text = { Text("确定要删除「${item.displayName}」吗？") },
            confirmButton = {
                Button(onClick = { viewModel.onEvent(FontPickerEvent.ConfirmDelete(item)) }) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(FontPickerEvent.DismissDeleteDialog) }) {
                    Text("取消")
                }
            }
        )
    }

    // ── URL 导入对话框 ──
    if (uiState.showUrlImportDialog) {
        UrlImportDialog(
            onDismiss = { viewModel.onEvent(FontPickerEvent.DismissUrlImportDialog) },
            onConfirm = { url, name ->
                viewModel.onEvent(FontPickerEvent.ConfirmUrlImport(url, name))
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("字体") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { showFabMenu = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "导入字体")
                }
                DropdownMenu(
                    expanded = showFabMenu,
                    onDismissRequest = { showFabMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("从手机选择文件") },
                        leadingIcon = { Icon(Icons.Filled.FileOpen, contentDescription = null) },
                        onClick = {
                            showFabMenu = false
                            filePickerLauncher.launch(arrayOf("font/*", "application/x-font-ttf", "application/x-font-opentype"))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("输入字体直链 URL") },
                        leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null) },
                        onClick = {
                            showFabMenu = false
                            viewModel.onEvent(FontPickerEvent.ShowUrlImportDialog)
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // ── Tab 切换 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FontTab.entries.forEach { tab ->
                    FilterChip(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.onEvent(FontPickerEvent.SwitchTab(tab)) },
                        label = { Text(tab.displayName) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            when {
                uiState.isLoading && uiState.fonts.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.fonts.isEmpty() -> {
                    Box(
                        Modifier.fillMaxSize().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无字体",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    // 按来源分组
                    val grouped = uiState.fonts.groupBy { it.source }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // 系统字体
                        grouped[FontSource.SYSTEM]?.let { fonts ->
                            item { SectionHeader("📱 系统字体") }
                            items(fonts, key = { it.id }) { font ->
                                FontCard(
                                    item = font,
                                    isSelected = uiState.selectedFontId == font.id,
                                    downloadProgress = null,
                                    onSelect = { viewModel.onEvent(FontPickerEvent.SelectFont(font.id)) },
                                    onDownload = { },
                                    onLongPress = { /* 系统字体不可删除 */ }
                                )
                            }
                        }

                        // 内置字体
                        grouped[FontSource.BUILT_IN]?.let { fonts ->
                            item { SectionHeader("📦 内置字体") }
                            items(fonts, key = { it.id }) { font ->
                                FontCard(
                                    item = font,
                                    isSelected = uiState.selectedFontId == font.id,
                                    downloadProgress = uiState.downloadingFonts[font.id],
                                    onSelect = { viewModel.onEvent(FontPickerEvent.SelectFont(font.id)) },
                                    onDownload = { viewModel.onEvent(FontPickerEvent.DownloadFont(font)) },
                                    onLongPress = { /* 内置字体不可删除 */ }
                                )
                            }
                        }

                        // 云端字体
                        grouped[FontSource.REMOTE]?.let { fonts ->
                            item { SectionHeader("☁️ 云端字体") }
                            items(fonts, key = { it.id }) { font ->
                                FontCard(
                                    item = font,
                                    isSelected = uiState.selectedFontId == font.id,
                                    downloadProgress = uiState.downloadingFonts[font.id],
                                    onSelect = { viewModel.onEvent(FontPickerEvent.SelectFont(font.id)) },
                                    onDownload = { viewModel.onEvent(FontPickerEvent.DownloadFont(font)) },
                                    onLongPress = {
                                        if (font.isDownloaded) {
                                            viewModel.onEvent(FontPickerEvent.ShowDeleteDialog(font))
                                        }
                                    }
                                )
                            }
                        }

                        // 我的字体（LOCAL + URL）
                        val myFonts = (grouped[FontSource.LOCAL].orEmpty() + grouped[FontSource.URL].orEmpty())
                        if (myFonts.isNotEmpty()) {
                            item { SectionHeader("📁 我的字体") }
                            items(myFonts, key = { it.id }) { font ->
                                FontCard(
                                    item = font,
                                    isSelected = uiState.selectedFontId == font.id,
                                    downloadProgress = null,
                                    onSelect = { viewModel.onEvent(FontPickerEvent.SelectFont(font.id)) },
                                    onDownload = { },
                                    onLongPress = {
                                        viewModel.onEvent(FontPickerEvent.ShowDeleteDialog(font))
                                    }
                                )
                            }
                        }

                        item { Spacer(Modifier.height(80.dp)) } // FAB 留白
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FontCard(
    item: FontItem,
    isSelected: Boolean,
    downloadProgress: Float?,
    onSelect: () -> Unit,
    onDownload: () -> Unit,
    onLongPress: () -> Unit
) {
    val isDownloading = downloadProgress != null
    val canSelect = item.isDownloaded || item.source == FontSource.BUILT_IN || item.source == FontSource.SYSTEM

    // 尝试加载字体用于预览
    val context = LocalContext.current
    val previewFontFamily: FontFamily? = remember(item.id, item.isDownloaded) {
        if (!canSelect) null
        else when (item.source) {
            FontSource.SYSTEM -> FontFamily.Default
            FontSource.BUILT_IN -> {
                try {
                    // 将 assets 字体复制到缓存后通过 Font(File) 加载
                    val cacheFile = File(context.cacheDir, "builtin_font/${item.fileName}")
                    if (!cacheFile.exists()) {
                        cacheFile.parentFile?.mkdirs()
                        context.assets.open("fonts/${item.fileName}").use { input ->
                            cacheFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    FontFamily(Font(cacheFile))
                } catch (_: Exception) {
                    null
                }
            }
            else -> {
                try {
                    val file = File(context.filesDir, "fonts/${item.fileName}")
                    if (file.exists()) FontFamily(Font(file))
                    else null
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (canSelect) onSelect() },
                onLongClick = onLongPress
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // RadioButton
            if (canSelect) {
                RadioButton(
                    selected = isSelected,
                    onClick = onSelect
                )
            } else {
                Spacer(Modifier.width(48.dp))
            }

            // 文字区域
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.style.isNotBlank()) {
                    Text(
                        text = item.style,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(4.dp))

                // 预览文字
                Text(
                    text = item.preview,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = previewFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.alpha(if (canSelect) 1f else 0.4f),
                    color = if (canSelect) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 进度条
                if (isDownloading) {
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress ?: 0f },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 标签
                if (item.tags.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.tags.joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // 右侧操作
            if (!canSelect && !isDownloading && item.source == FontSource.REMOTE) {
                OutlinedButton(onClick = onDownload) {
                    Icon(
                        Icons.Filled.CloudDownload,
                        contentDescription = "下载",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(formatFileSize(item.fileSize))
                }
            }
        }
    }
}

@Composable
private fun UrlImportDialog(
    onDismiss: () -> Unit,
    onConfirm: (url: String, name: String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("输入字体直链 URL") },
        text = {
            Column {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("字体文件 URL") },
                    placeholder = { Text("https://example.com/font.ttf") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("显示名称") },
                    placeholder = { Text("我的字体") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(url.trim(), name.trim().ifBlank { "自定义字体" }) },
                enabled = url.isNotBlank()
            ) {
                Text("导入")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
    }
}
