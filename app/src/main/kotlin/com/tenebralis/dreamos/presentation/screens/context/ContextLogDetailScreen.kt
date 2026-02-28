package com.tenebralis.dreamos.presentation.screens.context

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tenebralis.dreamos.domain.model.ContextLayer
import com.tenebralis.dreamos.domain.model.ContextSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextLogDetailScreen(
    onBack: () -> Unit,
    viewModel: ContextLogDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("上下文详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    state.log?.let { log ->
                        IconButton(onClick = { copyToClipboard(context, log.fullPromptText) }) {
                            Icon(Icons.Filled.ContentCopy, "复制全文")
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }

            state.log != null -> {
                val log = state.log!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 概览
                    item {
                        Card(shape = RoundedCornerShape(12.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("会话 ID", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(log.conversationId, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(8.dp))
                                Text("创建时间", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(log.createdAt.take(19).replace("T", " "))
                                Spacer(Modifier.height(8.dp))
                                Text("Token 估算", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${log.totalTokensEstimate} tokens", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 各层
                    item {
                        Text(
                            "上下文层级",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }

                    items(log.layers.entries.toList(), key = { it.key }) { (layerId, layer) ->
                        LayerCard(
                            layerId = layerId,
                            layer = layer,
                            isExpanded = layerId in state.expandedLayers,
                            onToggle = { viewModel.toggleLayer(layerId) }
                        )
                    }

                    // 完整 Prompt
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleFullPrompt() },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📄  完整 Prompt", fontWeight = FontWeight.Bold)
                                    Icon(
                                        if (state.showFullPrompt) Icons.Filled.ExpandLess
                                        else Icons.Filled.ExpandMore,
                                        contentDescription = null
                                    )
                                }
                                AnimatedVisibility(visible = state.showFullPrompt) {
                                    Text(
                                        log.fullPromptText,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 8.dp),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LayerCard(
    layerId: String,
    layer: ContextLayer,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val icon = ContextSettings.LAYER_ICONS[layerId] ?: "📦"
    val name = ContextSettings.LAYER_DISPLAY_NAMES[layerId] ?: layerId

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (layer.enabled)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$icon  ", fontSize = 16.sp)
                    Text(name, fontWeight = FontWeight.Medium)
                    if (!layer.enabled) {
                        Text(
                            "  已禁用",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${layer.tokens} tok",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    layer.count?.let { count ->
                        Text(
                            "  · $count 条",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Text(
                    layer.content ?: "(空)",
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 8.dp),
                    lineHeight = 16.sp,
                    color = if (layer.content != null) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("context_prompt", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
}
