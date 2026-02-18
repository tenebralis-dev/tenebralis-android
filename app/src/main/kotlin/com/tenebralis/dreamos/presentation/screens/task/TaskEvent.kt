package com.tenebralis.dreamos.presentation.screens.task

/**
 * 任务页面事件
 */
sealed interface TaskEvent {
    data object Refresh : TaskEvent
    data class SwitchTab(val tab: TaskTab) : TaskEvent
    data class StartTask(val taskId: String) : TaskEvent
    data class CompleteTask(val userTaskId: String) : TaskEvent
    data object DismissError : TaskEvent
}
