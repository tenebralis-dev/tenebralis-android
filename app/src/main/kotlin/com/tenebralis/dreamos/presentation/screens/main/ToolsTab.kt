package com.tenebralis.dreamos.presentation.screens.main

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tenebralis.dreamos.presentation.navigation.Screen

/**
 * 工具 Tab
 *
 * 整合 NPC 管理、预设管理、API 连接、上下文、记忆、备忘录/日历/番茄钟。
 */
@Composable
fun ToolsTab(
    modifier: Modifier = Modifier,
    onNavigateRoute: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "工具",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // ── AI 配置 ──
        ToolsSectionTitle("AI 配置")

        ToolsItem(
            icon = Icons.Filled.Person,
            title = "角色卡",
            subtitle = "管理 NPC 人设",
            onClick = { onNavigateRoute(Screen.NpcList.route) }
        )
        ToolsItem(
            icon = Icons.Filled.Layers,
            title = "预设",
            subtitle = "管理 AI 采样参数",
            onClick = { onNavigateRoute(Screen.Preset.route) }
        )
        ToolsItem(
            icon = Icons.Filled.Link,
            title = "连接",
            subtitle = "API 连接管理",
            onClick = { onNavigateRoute(Screen.Connection.route) }
        )
        ToolsItem(
            icon = Icons.Filled.DataObject,
            title = "上下文",
            subtitle = "查看上下文日志",
            onClick = { onNavigateRoute(Screen.Context.route) }
        )
        ToolsItem(
            icon = Icons.Filled.Bookmark,
            title = "记忆",
            subtitle = "管理长期记忆",
            onClick = { onNavigateRoute(Screen.Memory.route) }
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        // ── 效率工具 ──
        ToolsSectionTitle("效率工具")

        ToolsItem(
            icon = Icons.Filled.Description,
            title = "备忘",
            subtitle = "快速记录笔记",
            onClick = { onNavigateRoute(Screen.Notes.route) }
        )
        ToolsItem(
            icon = Icons.Filled.Event,
            title = "日历",
            subtitle = "日程管理",
            onClick = { onNavigateRoute(Screen.Calendar.route) }
        )
        ToolsItem(
            icon = Icons.Filled.Timer,
            title = "番茄钟",
            subtitle = "专注计时器",
            onClick = { onNavigateRoute(Screen.Pomodoro.route) }
        )
    }
}

@Composable
private fun ToolsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun ToolsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
