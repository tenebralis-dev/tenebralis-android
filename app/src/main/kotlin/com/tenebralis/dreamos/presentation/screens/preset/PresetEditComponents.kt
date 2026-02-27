package com.tenebralis.dreamos.presentation.screens.preset

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * 预设编辑器通用 UI 组件
 */

// ─── 可折叠分区容器 ──────────────────────────────────────────

@Composable
fun CollapsibleSection(
    title: String,
    icon: String = "",
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 标题栏（可点击折叠/展开）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon.isNotEmpty()) {
                    Text(
                        text = icon,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "收起" else "展开",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 内容区（动画展开/收起）
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    content()
                }
            }
        }
    }
}

// ─── 滑块 + 文本输入行 ──────────────────────────────────────

@Composable
fun ParamSliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    decimalPlaces: Int = 2
) {
    // 本地文本状态：允许用户自由删除和输入
    var textValue by remember { mutableStateOf(String.format("%.${decimalPlaces}f", value)) }
    // 当外部 value 变化（如滑块拖动）时，同步文本
    LaunchedEffect(value) {
        val formatted = String.format("%.${decimalPlaces}f", value)
        if (textValue.toFloatOrNull() != value) {
            textValue = formatted
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = textValue,
                onValueChange = { text ->
                    textValue = text
                    // 实时提交有效值
                    text.toFloatOrNull()?.let { v ->
                        if (v in valueRange) onValueChange(v)
                    }
                },
                modifier = Modifier
                    .width(80.dp)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            // 失焦时：如果文本无效，重置为当前值
                            val parsed = textValue.toFloatOrNull()
                            if (parsed == null || parsed !in valueRange) {
                                textValue = String.format("%.${decimalPlaces}f", value)
                            }
                        }
                    },
                textStyle = MaterialTheme.typography.bodySmall,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                )
            )
        }
        Slider(
            value = value.coerceIn(valueRange),
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ─── 整数滑块行 ──────────────────────────────────────────

@Composable
fun ParamIntSliderRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange,
    step: Int = 1
) {
    var textValue by remember { mutableStateOf(value.toString()) }
    LaunchedEffect(value) {
        if (textValue.toIntOrNull() != value) {
            textValue = value.toString()
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = textValue,
                onValueChange = { text ->
                    textValue = text
                    text.toIntOrNull()?.let { v ->
                        if (v in valueRange) onValueChange(v)
                    }
                },
                modifier = Modifier
                    .width(100.dp)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            val parsed = textValue.toIntOrNull()
                            if (parsed == null || parsed !in valueRange) {
                                textValue = value.toString()
                            }
                        }
                    },
                textStyle = MaterialTheme.typography.bodySmall,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            steps = ((valueRange.last - valueRange.first) / step).coerceAtMost(100),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ─── 开关行 ──────────────────────────────────────────────

@Composable
fun ParamSwitchRow(
    label: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

// ─── 下拉选择行 ──────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParamDropdownRow(
    label: String,
    selectedValue: String,
    options: List<Pair<String, String>>,  // value to displayLabel
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayLabel = options.find { it.first == selectedValue }?.second ?: selectedValue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = displayLabel,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                textStyle = MaterialTheme.typography.bodySmall,
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { (value, display) ->
                    DropdownMenuItem(
                        text = { Text(display) },
                        onClick = {
                            onValueChange(value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// ─── 多行文本输入行 ──────────────────────────────────────

@Composable
fun ParamTextFieldRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = 5
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodySmall,
            singleLine = singleLine,
            minLines = if (singleLine) 1 else minLines,
            maxLines = if (singleLine) 1 else maxLines
        )
    }
}

// ─── 只读信息行 ──────────────────────────────────────────

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
