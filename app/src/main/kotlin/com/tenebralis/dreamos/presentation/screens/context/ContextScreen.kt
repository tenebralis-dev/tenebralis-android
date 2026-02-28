package com.tenebralis.dreamos.presentation.screens.context

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tenebralis.dreamos.domain.model.ContextLog
import com.tenebralis.dreamos.domain.model.ContextSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextScreen(
    onBack: () -> Unit,
    onLogClick: (Long) -> Unit,
    viewModel: ContextViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val tabTitles = listOf("日志", "设置")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("上下文管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    if (state.selectedTab == 0) {
                        IconButton(onClick = { viewModel.onEvent(ContextEvent.CleanOldLogs) }) {
                            Icon(Icons.Filled.CleaningServices, "清理过期")
                        }
                        IconButton(onClick = { viewModel.onEvent(ContextEvent.ClearAllLogs) }) {
                            Icon(Icons.Filled.DeleteSweep, "清空全部")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // 概览卡片
            OverviewCard(state)

            // Tab
            TabRow(selectedTabIndex = state.selectedTab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.onEvent(ContextEvent.SelectTab(index)) },
                        text = { Text(title) }
                    )
                }
            }

            when (state.selectedTab) {
                0 -> LogsTab(state, onLogClick)
                1 -> SettingsTab(state, viewModel::onEvent)
            }

            // Info / Error Snackbar
            state.infoMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.onEvent(ContextEvent.ClearInfo) }) {
                            Text("关闭")
                        }
                    }
                ) { Text(msg) }
            }
            state.errorMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    action = {
                        TextButton(onClick = { viewModel.onEvent(ContextEvent.ClearError) }) {
                            Text("关闭")
                        }
                    }
                ) { Text(msg, color = MaterialTheme.colorScheme.onErrorContainer) }
            }
        }
    }
}

@Composable
private fun OverviewCard(state: ContextUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("日志总数", state.logCount.toString())
            StatItem("近期消息", state.settings.recentMessageCount.toString())
            StatItem("记忆 TopN", state.settings.memoryTopN.toString())
            StatItem("Token 上限", state.settings.maxTokenEstimate.toString())
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LogsTab(state: ContextUiState, onLogClick: (Long) -> Unit) {
    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.logs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无上下文日志", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(state.logs, key = { it.id }) { log ->
            LogItem(log, onClick = { onLogClick(log.id) })
        }
    }
}

@Composable
private fun LogItem(log: ContextLog, onClick: () -> Unit) {
    val tokenColor = when {
        log.totalTokensEstimate > 6000 -> Color(0xFFE57373)  // 红
        log.totalTokensEstimate > 3000 -> Color(0xFFFFB74D)  // 橙
        else -> Color(0xFF81C784)                             // 绿
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "会话: ${log.conversationId.take(8)}…",
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    log.createdAt.take(19).replace("T", " "),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Token 指示
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(tokenColor.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "${log.totalTokensEstimate} tokens",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tokenColor
                )
            }
        }
    }
}

@Composable
private fun SettingsTab(state: ContextUiState, onEvent: (ContextEvent) -> Unit) {
    val settings = state.settings

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 消息数量
        item {
            SliderSettingRow(
                label = "近期消息数量",
                value = settings.recentMessageCount.toFloat(),
                valueRange = 5f..100f,
                steps = 18,
                valueLabel = settings.recentMessageCount.toString(),
                onValueChange = { onEvent(ContextEvent.UpdateRecentMessageCount(it.toInt())) }
            )
        }

        // 记忆 TopN
        item {
            SliderSettingRow(
                label = "记忆召回数量",
                value = settings.memoryTopN.toFloat(),
                valueRange = 0f..50f,
                steps = 9,
                valueLabel = settings.memoryTopN.toString(),
                onValueChange = { onEvent(ContextEvent.UpdateMemoryTopN(it.toInt())) }
            )
        }

        // Token 上限
        item {
            SliderSettingRow(
                label = "Token 上限估算",
                value = settings.maxTokenEstimate.toFloat(),
                valueRange = 1024f..32768f,
                steps = 30,
                valueLabel = settings.maxTokenEstimate.toString(),
                onValueChange = { onEvent(ContextEvent.UpdateMaxTokenEstimate(it.toInt())) }
            )
        }

        // 自动记录
        item {
            SwitchSettingRow(
                label = "自动记录上下文",
                checked = settings.autoLogEnabled,
                onCheckedChange = { onEvent(ContextEvent.ToggleAutoLog) }
            )
        }

        // 日志保留天数
        item {
            SliderSettingRow(
                label = "日志保留天数",
                value = settings.logRetentionDays.toFloat(),
                valueRange = 1f..90f,
                steps = 88,
                valueLabel = "${settings.logRetentionDays} 天",
                onValueChange = { onEvent(ContextEvent.UpdateLogRetentionDays(it.toInt())) }
            )
        }

        // 层级开关
        item {
            Text(
                "上下文层级",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        ContextSettings.ALL_LAYERS.forEach { layerId ->
            item(key = layerId) {
                val icon = ContextSettings.LAYER_ICONS[layerId] ?: "📦"
                val name = ContextSettings.LAYER_DISPLAY_NAMES[layerId] ?: layerId
                SwitchSettingRow(
                    label = "$icon  $name",
                    checked = layerId in settings.enabledLayers,
                    onCheckedChange = { onEvent(ContextEvent.ToggleLayer(layerId)) }
                )
            }
        }
    }
}

@Composable
private fun SliderSettingRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueLabel: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontWeight = FontWeight.Medium)
            Text(valueLabel, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SwitchSettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.Medium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
