package com.tenebralis.dreamos.presentation.screens.achievement

/**
 * 成就页面事件
 */
sealed interface AchievementEvent {
    data object Refresh : AchievementEvent
    data class SwitchTab(val tab: AchievementTab) : AchievementEvent
    data object DismissError : AchievementEvent
}
