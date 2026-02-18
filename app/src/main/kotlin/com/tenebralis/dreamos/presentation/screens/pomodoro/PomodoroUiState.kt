package com.tenebralis.dreamos.presentation.screens.pomodoro

import com.tenebralis.dreamos.domain.model.PomodoroSession

/**
 * 番茄钟 UI 状态
 */
data class PomodoroUiState(
    val timerStatus: TimerStatus = TimerStatus.IDLE,
    val totalSeconds: Int = 25 * 60,        // 总秒数
    val remainingSeconds: Int = 25 * 60,     // 剩余秒数
    val durationMinutes: Int = 25,           // 可调节的时长
    val taskDescription: String = "",
    val todaySessions: List<PomodoroSession> = emptyList(),
    val weekSessions: List<PomodoroSession> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
) {
    /** 进度 0f..1f */
    val progress: Float
        get() = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 0f

    /** 格式化剩余时间 */
    val formattedTime: String
        get() {
            val m = remainingSeconds / 60
            val s = remainingSeconds % 60
            return "%02d:%02d".format(m, s)
        }

    /** 今日完成数 */
    val todayCompletedCount: Int get() = todaySessions.size

    /** 今日总专注分钟 */
    val todayTotalMinutes: Int get() = todaySessions.sumOf { it.durationMinutes }

    /** 本周完成数 */
    val weekCompletedCount: Int get() = weekSessions.size

    /** 本周总专注分钟 */
    val weekTotalMinutes: Int get() = weekSessions.sumOf { it.durationMinutes }
}

enum class TimerStatus { IDLE, RUNNING, PAUSED, COMPLETED }
