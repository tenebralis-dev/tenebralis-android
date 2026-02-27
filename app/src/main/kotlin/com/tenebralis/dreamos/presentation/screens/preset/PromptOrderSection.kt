package com.tenebralis.dreamos.presentation.screens.preset

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * 提示词排列顺序分区
 *
 * 使用上下箭头按钮实现排序，每组 prompt_order 显示为一个标签页。
 */

@Composable
fun PromptOrderSection(
    groups: List<PromptOrderGroup>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onMoveUp: (groupIndex: Int, itemIndex: Int) -> Unit,
    onMoveDown: (groupIndex: Int, itemIndex: Int) -> Unit,
    onToggleEnabled: (groupIndex: Int, itemIndex: Int, enabled: Boolean) -> Unit
) {
    CollapsibleSection(
        title = "提示词排列顺序",
        icon = "🔢",
        expanded = expanded,
        onToggle = onToggle
    ) {
        groups.forEachIndexed { groupIndex, group ->
            // 组标题
            val groupLabel = when (group.characterId) {
                100000 -> "默认排序 (100000)"
                100001 -> "自定义排序 (100001)"
                else -> "排序组 (${group.characterId})"
            }

            Text(
                text = groupLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = if (groupIndex > 0) 8.dp else 0.dp)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            // 排序项列表
            group.orders.forEachIndexed { itemIndex, entry ->
                PromptOrderRow(
                    entry = entry,
                    isFirst = itemIndex == 0,
                    isLast = itemIndex == group.orders.lastIndex,
                    onMoveUp = { onMoveUp(groupIndex, itemIndex) },
                    onMoveDown = { onMoveDown(groupIndex, itemIndex) },
                    onToggleEnabled = { enabled ->
                        onToggleEnabled(groupIndex, itemIndex, enabled)
                    }
                )
                if (itemIndex < group.orders.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PromptOrderRow(
    entry: PromptOrderEntry,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 上下箭头
        IconButton(
            onClick = onMoveUp,
            enabled = !isFirst,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = "上移",
                modifier = Modifier.size(18.dp),
                tint = if (!isFirst) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
        }

        IconButton(
            onClick = onMoveDown,
            enabled = !isLast,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "下移",
                modifier = Modifier.size(18.dp),
                tint = if (!isLast) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // 名称
        Text(
            text = entry.displayName.ifEmpty { entry.identifier },
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            color = if (entry.enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        // 启用开关
        Switch(
            checked = entry.enabled,
            onCheckedChange = onToggleEnabled
        )
    }
}
