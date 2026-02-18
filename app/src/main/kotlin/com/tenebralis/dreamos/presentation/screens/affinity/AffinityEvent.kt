package com.tenebralis.dreamos.presentation.screens.affinity

/**
 * 好感度页面事件
 */
sealed interface AffinityEvent {
    data object Refresh : AffinityEvent
    data object DismissError : AffinityEvent
}
