package com.tenebralis.dreamos.presentation.screens.preset

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Prompt 编辑弹窗
 *
 * 支持编辑名称、角色、内容、注入设置。
 * marker 类型的 prompt 仅显示名称和 identifier（不可编辑内容）。
 */

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PromptEditDialog(
    prompt: EditablePrompt,
    onDismiss: () -> Unit,
    onConfirm: (EditablePrompt) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var editedPrompt by remember(prompt) { mutableStateOf(prompt) }
    val roles = listOf("system", "user", "assistant")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (prompt.marker) "查看标记" else "编辑提示词",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 名称
                OutlinedTextField(
                    value = editedPrompt.name,
                    onValueChange = { editedPrompt = editedPrompt.copy(name = it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("名称") },
                    singleLine = true
                )

                // Identifier（只读）
                OutlinedTextField(
                    value = editedPrompt.identifier,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Identifier") },
                    readOnly = true,
                    singleLine = true,
                    enabled = false
                )

                if (!prompt.marker) {
                    // 角色选择
                    Text(
                        text = "角色",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        roles.forEachIndexed { index, role ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = roles.size
                                ),
                                onClick = { editedPrompt = editedPrompt.copy(role = role) },
                                selected = editedPrompt.role == role
                            ) {
                                Text(role)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 内容
                    OutlinedTextField(
                        value = editedPrompt.content,
                        onValueChange = { editedPrompt = editedPrompt.copy(content = it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        label = { Text("内容") },
                        minLines = 6
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 注入设置
                    Text(
                        text = "注入设置",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ParamIntSliderRow(
                        label = "注入深度",
                        value = editedPrompt.injectionDepth,
                        onValueChange = { editedPrompt = editedPrompt.copy(injectionDepth = it) },
                        valueRange = 0..10
                    )

                    ParamIntSliderRow(
                        label = "注入顺序",
                        value = editedPrompt.injectionOrder,
                        onValueChange = { editedPrompt = editedPrompt.copy(injectionOrder = it) },
                        valueRange = 0..999
                    )

                    ParamSwitchRow(
                        label = "禁止覆盖",
                        checked = editedPrompt.forbidOverrides,
                        onCheckedChange = { editedPrompt = editedPrompt.copy(forbidOverrides = it) }
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (onDelete != null && !prompt.marker) {
                    TextButton(onClick = onDelete) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
                TextButton(onClick = { onConfirm(editedPrompt) }) {
                    Text("确定")
                }
            }
        }
    )
}
