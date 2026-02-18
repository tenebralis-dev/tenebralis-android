package com.tenebralis.dreamos.presentation.screens.pomodoro

/**
 * 番茄钟 UI 事件
 */
sealed interface PomodoroEvent {
    data object Start : PomodoroEvent
    data object Pause : PomodoroEvent
    data object Resume : PomodoroEvent
    data object Reset : PomodoroEvent
    data object Complete : PomodoroEvent          // 计时归零触发
    data class DurationChanged(val minutes: Int) : PomodoroEvent
    data class TaskDescriptionChanged(val value: String) : PomodoroEvent
    data object Refresh : PomodoroEvent

    data object ClearError : PomodoroEvent
    data object ClearInfo : PomodoroEvent
}
