package com.tenebralis.dreamos.presentation.screens.achievement

import com.tenebralis.dreamos.domain.model.UserAchievement

/**
 * 成就页面 UI 状态
 */
data class AchievementUiState(
    val userAchievements: List<UserAchievement> = emptyList(),
    val selectedTab: AchievementTab = AchievementTab.ALL,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class AchievementTab(val displayName: String) {
    ALL("全部"),
    UNLOCKED("已解锁"),
    LOCKED("未解锁")
}
