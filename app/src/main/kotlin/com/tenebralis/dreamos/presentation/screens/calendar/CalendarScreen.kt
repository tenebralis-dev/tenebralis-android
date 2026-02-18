package com.tenebralis.dreamos.presentation.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenebralis.dreamos.domain.model.UserCalendarEvent
import com.tenebralis.dreamos.domain.model.enums.AiVisibility
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBackClick: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(CalendarUiEvent.ClearError)
        }
    }
    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(CalendarUiEvent.ClearInfo)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日历") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data -> Snackbar(snackbarData = data) }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(CalendarUiEvent.StartCreate) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "新增日程", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ─── 月份切换 ──────────────────────────────
            MonthHeader(
                yearMonth = uiState.currentMonth,
                onPrevious = {
                    viewModel.onEvent(CalendarUiEvent.MonthChanged(uiState.currentMonth.minusMonths(1)))
                },
                onNext = {
                    viewModel.onEvent(CalendarUiEvent.MonthChanged(uiState.currentMonth.plusMonths(1)))
                }
            )

            // ─── 星期标题 ──────────────────────────────
            WeekDayHeader()

            // ─── 月历网格 ──────────────────────────────
            MonthGrid(
                yearMonth = uiState.currentMonth,
                selectedDate = uiState.selectedDate,
                datesWithEvents = uiState.datesWithEvents,
                onDateSelected = { viewModel.onEvent(CalendarUiEvent.DateSelected(it)) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ─── 选中日期标题 ──────────────────────────
            Text(
                text = "── ${uiState.selectedDate.monthValue}月${uiState.selectedDate.dayOfMonth}日 ──",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ─── 日程列表 ──────────────────────────────
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.selectedDateEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无日程",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.selectedDateEvents,
                        key = { it.id }
                    ) { event ->
                        EventCard(
                            event = event,
                            onEdit = { viewModel.onEvent(CalendarUiEvent.StartEdit(event)) },
                            onDelete = { viewModel.onEvent(CalendarUiEvent.Delete(event.id)) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // ─── 编辑弹窗 ──────────────────────────────────────
        if (uiState.isEditSheetVisible) {
            CalendarEditSheet(
                isCreating = uiState.isCreating,
                title = uiState.editTitle,
                description = uiState.editDescription,
                date = uiState.editDate,
                startTime = uiState.editStartTime,
                endTime = uiState.editEndTime,
                isAllDay = uiState.editIsAllDay,
                repeatRule = uiState.editRepeatRule,
                aiVisibility = uiState.editAiVisibility,
                isSaving = uiState.isSaving,
                onTitleChanged = { viewModel.onEvent(CalendarUiEvent.TitleChanged(it)) },
                onDescriptionChanged = { viewModel.onEvent(CalendarUiEvent.DescriptionChanged(it)) },
                onDateChanged = { viewModel.onEvent(CalendarUiEvent.DateChanged(it)) },
                onStartTimeChanged = { viewModel.onEvent(CalendarUiEvent.StartTimeChanged(it)) },
                onEndTimeChanged = { viewModel.onEvent(CalendarUiEvent.EndTimeChanged(it)) },
                onAllDayChanged = { viewModel.onEvent(CalendarUiEvent.AllDayChanged(it)) },
                onRepeatRuleChanged = { viewModel.onEvent(CalendarUiEvent.RepeatRuleChanged(it)) },
                onAiVisibilityChanged = { viewModel.onEvent(CalendarUiEvent.AiVisibilityChanged(it)) },
                onSave = { viewModel.onEvent(CalendarUiEvent.SaveEvent) },
                onDismiss = { viewModel.onEvent(CalendarUiEvent.DismissEdit) }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  子组件
// ═══════════════════════════════════════════════════════════════

@Composable
private fun MonthHeader(
    yearMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "上一月")
        }
        Text(
            text = "${yearMonth.year}年${yearMonth.monthValue}月",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "下一月")
        }
    }
}

@Composable
private fun WeekDayHeader() {
    val days = listOf("日", "一", "二", "三", "四", "五", "六")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        days.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MonthGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    datesWithEvents: Set<Int>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    // Sunday = 0, Monday = 1, ...
    val startOffset = firstDayOfMonth.dayOfWeek.value % 7
    val totalCells = startOffset + daysInMonth

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        userScrollEnabled = false
    ) {
        // 空白占位
        items(startOffset) {
            Box(modifier = Modifier.aspectRatio(1f))
        }
        // 日期格子
        items(daysInMonth) { index ->
            val day = index + 1
            val date = yearMonth.atDay(day)
            val isSelected = date == selectedDate
            val isToday = date == LocalDate.now()
            val hasEvent = day in datesWithEvents

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            isToday -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surface
                        }
                    )
                    .clickable { onDateSelected(date) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = day.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isToday -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (hasEvent) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.primary
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    event: UserCalendarEvent,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "📅 ${event.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                val timeText = if (event.isAllDay) {
                    "全天"
                } else {
                    val start = if (event.startAt.length >= 16) event.startAt.substring(11, 16) else ""
                    val end = event.endAt?.let { if (it.length >= 16) it.substring(11, 16) else "" } ?: ""
                    if (end.isNotEmpty()) "$start - $end" else start
                }
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // 重复规则 + AI可见性
                Row(verticalAlignment = Alignment.CenterVertically) {
                    event.repeatRule?.let { rule ->
                        val ruleLabel = when (rule) {
                            "daily" -> "🔄 每天"
                            "weekly" -> "🔄 每周"
                            "monthly" -> "🔄 每月"
                            else -> null
                        }
                        ruleLabel?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                    if (event.aiVisibility != AiVisibility.PRIVATE) {
                        Text(
                            text = "👁️ ${event.aiVisibility.name.lowercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "删除",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarEditSheet(
    isCreating: Boolean,
    title: String,
    description: String,
    date: String,
    startTime: String,
    endTime: String,
    isAllDay: Boolean,
    repeatRule: String,
    aiVisibility: AiVisibility,
    isSaving: Boolean,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onDateChanged: (String) -> Unit,
    onStartTimeChanged: (String) -> Unit,
    onEndTimeChanged: (String) -> Unit,
    onAllDayChanged: (Boolean) -> Unit,
    onRepeatRuleChanged: (String) -> Unit,
    onAiVisibilityChanged: (AiVisibility) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = if (isCreating) "新增日程" else "编辑日程",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 标题
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("标题") },
                singleLine = true,
                enabled = !isSaving
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 描述
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("描述（可选）") },
                maxLines = 3,
                enabled = !isSaving
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 日期
            OutlinedTextField(
                value = date,
                onValueChange = onDateChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("日期 (yyyy-MM-dd)") },
                singleLine = true,
                enabled = !isSaving
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 全天开关
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isAllDay,
                    onCheckedChange = onAllDayChanged,
                    enabled = !isSaving
                )
                Text("全天", style = MaterialTheme.typography.bodyMedium)
            }

            // 时间（非全天时显示）
            if (!isAllDay) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = onStartTimeChanged,
                        modifier = Modifier.weight(1f),
                        label = { Text("开始 (HH:mm)") },
                        singleLine = true,
                        enabled = !isSaving
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = onEndTimeChanged,
                        modifier = Modifier.weight(1f),
                        label = { Text("结束 (HH:mm)") },
                        singleLine = true,
                        enabled = !isSaving
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 重复规则下拉
            RepeatRuleDropdown(
                selected = repeatRule,
                onChanged = onRepeatRuleChanged,
                enabled = !isSaving
            )
            Spacer(modifier = Modifier.height(8.dp))

            // AI 可见性下拉
            CalendarAiVisibilityDropdown(
                selected = aiVisibility,
                onChanged = onAiVisibilityChanged,
                enabled = !isSaving
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 保存/取消
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss, enabled = !isSaving) {
                    Text("取消")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = onSave,
                    enabled = !isSaving && title.isNotBlank()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("保存")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepeatRuleDropdown(
    selected: String,
    onChanged: (String) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val options = mapOf(
        "none" to "不重复",
        "daily" to "每天",
        "weekly" to "每周",
        "monthly" to "每月"
    )

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (enabled) expanded = it }) {
        OutlinedTextField(
            value = options[selected] ?: selected,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            label = { Text("重复规则") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            enabled = enabled
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = { onChanged(key); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarAiVisibilityDropdown(
    selected: AiVisibility,
    onChanged: (AiVisibility) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val labels = mapOf(
        AiVisibility.PRIVATE to "🔒 私密",
        AiVisibility.ASSISTANT to "🤖 助手可见",
        AiVisibility.WORLD_CONTEXT to "🌍 世界上下文",
        AiVisibility.SAVE_CONTEXT to "💾 存档上下文"
    )

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (enabled) expanded = it }) {
        OutlinedTextField(
            value = labels[selected] ?: selected.name,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            label = { Text("AI 可见性") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            enabled = enabled
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AiVisibility.entries.forEach { visibility ->
                DropdownMenuItem(
                    text = { Text(labels[visibility] ?: visibility.name) },
                    onClick = { onChanged(visibility); expanded = false }
                )
            }
        }
    }
}
