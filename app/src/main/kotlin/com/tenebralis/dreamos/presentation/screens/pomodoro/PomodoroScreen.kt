package com.tenebralis.dreamos.presentation.screens.pomodoro

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenebralis.dreamos.domain.model.PomodoroSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    onBackClick: () -> Unit,
    viewModel: PomodoroViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(PomodoroEvent.ClearError)
        }
    }
    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(PomodoroEvent.ClearInfo)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("番茄钟") },
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
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ─── 计时器 ──────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                TimerRing(
                    progress = uiState.progress,
                    formattedTime = uiState.formattedTime,
                    modifier = Modifier.size(220.dp)
                )
            }

            // ─── 控制按钮 ────────────────────────────
            item {
                TimerControls(
                    status = uiState.timerStatus,
                    onStart = { viewModel.onEvent(PomodoroEvent.Start) },
                    onPause = { viewModel.onEvent(PomodoroEvent.Pause) },
                    onResume = { viewModel.onEvent(PomodoroEvent.Resume) },
                    onReset = { viewModel.onEvent(PomodoroEvent.Reset) }
                )
            }

            // ─── 时长调节（仅 IDLE 时可用）─────────
            if (uiState.timerStatus == TimerStatus.IDLE) {
                item {
                    DurationSelector(
                        minutes = uiState.durationMinutes,
                        onChanged = { viewModel.onEvent(PomodoroEvent.DurationChanged(it)) }
                    )
                }
            }

            // ─── 任务描述（仅 IDLE / RUNNING 时可用）
            if (uiState.timerStatus != TimerStatus.COMPLETED) {
                item {
                    OutlinedTextField(
                        value = uiState.taskDescription,
                        onValueChange = { viewModel.onEvent(PomodoroEvent.TaskDescriptionChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("在做什么？（可选）") },
                        singleLine = true,
                        enabled = uiState.timerStatus == TimerStatus.IDLE || uiState.timerStatus == TimerStatus.RUNNING,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // ─── 统计概览 ────────────────────────────
            item {
                StatsOverview(
                    todayCount = uiState.todayCompletedCount,
                    todayMinutes = uiState.todayTotalMinutes,
                    weekCount = uiState.weekCompletedCount,
                    weekMinutes = uiState.weekTotalMinutes
                )
            }

            // ─── 今日记录 ────────────────────────────
            if (uiState.todaySessions.isNotEmpty()) {
                item {
                    Text(
                        text = "今日记录",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }

                items(
                    items = uiState.todaySessions,
                    key = { it.id }
                ) { session ->
                    SessionCard(session = session)
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  子组件
// ═══════════════════════════════════════════════════════════════

@Composable
private fun TimerRing(
    progress: Float,
    formattedTime: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "timerProgress"
    )
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // 背景轨道
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 进度弧线
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Text(
            text = formattedTime,
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TimerControls(
    status: TimerStatus,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (status) {
            TimerStatus.IDLE -> {
                FilledIconButton(
                    onClick = onStart,
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "开始", modifier = Modifier.size(28.dp))
                }
            }
            TimerStatus.RUNNING -> {
                FilledIconButton(
                    onClick = onPause,
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.Pause, contentDescription = "暂停", modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(onClick = onReset) {
                    Icon(Icons.Filled.Refresh, contentDescription = "重置")
                }
            }
            TimerStatus.PAUSED -> {
                FilledIconButton(
                    onClick = onResume,
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "继续", modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(onClick = onReset) {
                    Icon(Icons.Filled.Refresh, contentDescription = "重置")
                }
            }
            TimerStatus.COMPLETED -> {
                FilledIconButton(
                    onClick = onReset,
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "重新开始", modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
private fun DurationSelector(
    minutes: Int,
    onChanged: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "专注时长：${minutes} 分钟",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Slider(
            value = minutes.toFloat(),
            onValueChange = { onChanged(it.toInt()) },
            valueRange = 5f..60f,
            steps = 10,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StatsOverview(
    todayCount: Int,
    todayMinutes: Int,
    weekCount: Int,
    weekMinutes: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "今日",
            count = todayCount,
            minutes = todayMinutes,
            emoji = "🍅",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "本周",
            count = weekCount,
            minutes = weekMinutes,
            emoji = "📈",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    minutes: Int,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "$emoji $title", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${count}个",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${minutes}分钟",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SessionCard(session: PomodoroSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🍅", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.taskDescription ?: "专注时间",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val time = if (session.startedAt.length >= 16)
                    session.startedAt.substring(11, 16) else ""
                Text(
                    text = "${session.durationMinutes}分钟  ·  $time",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
