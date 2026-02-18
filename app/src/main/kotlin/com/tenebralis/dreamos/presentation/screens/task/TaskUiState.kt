package com.tenebralis.dreamos.presentation.screens.task

import com.tenebralis.dreamos.domain.model.UserTask

/**
 * 任务页面 UI 状态
 */
data class TaskUiState(
    val userTasks: List<UserTask> = emptyList(),
    val selectedTab: TaskTab = TaskTab.ALL,
    val isLoading: Boolean = false,
    val error: String? = null,
    /** 任务完成后的奖励提示消息 */
    val rewardMessage: String? = null
)

enum class TaskTab(val displayName: String) {
    ALL("全部"),
    IN_PROGRESS("进行中"),
    COMPLETED("已完成")
}
