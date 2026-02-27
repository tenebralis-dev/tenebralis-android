package com.tenebralis.dreamos.presentation.screens.preset

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * 提示词列表分区
 */

@Composable
fun PromptListSection(
    prompts: List<EditablePrompt>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onEditPrompt: (index: Int) -> Unit,
    onToggleEnabled: (index: Int, enabled: Boolean) -> Unit,
    onInsertBelow: (index: Int) -> Unit,
    onAddPrompt: () -> Unit
) {
    CollapsibleSection(
        title = "提示词列表",
        icon = "📋",
        expanded = expanded,
        onToggle = onToggle
    ) {
        // 列头
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "名称",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "启用",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )

        // Prompt 列表
        prompts.forEachIndexed { index, prompt ->
            PromptItemRow(
                prompt = prompt,
                onEdit = { onEditPrompt(index) },
                onInsertBelow = { onInsertBelow(index) },
                onToggleEnabled = { enabled -> onToggleEnabled(index, enabled) }
            )
            if (index < prompts.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )
            }
        }

        // 添加按钮
        TextButton(
            onClick = onAddPrompt,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("添加提示词")
        }
    }
}

@Composable
private fun PromptItemRow(
    prompt: EditablePrompt,
    onEdit: () -> Unit,
    onInsertBelow: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 类型图标
        val typeIcon = when {
            prompt.marker -> "📌"
            prompt.systemPrompt && prompt.role == "system" -> "🔒"
            prompt.role == "user" -> "👤"
            prompt.role == "assistant" -> "🤖"
            else -> "📄"
        }
        Text(
            text = typeIcon,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 名称 + 角色
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = prompt.name.ifEmpty { prompt.identifier },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!prompt.marker) {
                Text(
                    text = prompt.role,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 编辑按钮
        IconButton(
            onClick = onEdit,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "编辑",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 插入按钮
        IconButton(
            onClick = onInsertBelow,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.PlaylistAdd,
                contentDescription = "在下方插入",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        }

        // 启用/禁用开关
        Switch(
            checked = prompt.enabled,
            onCheckedChange = onToggleEnabled,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
