package com.tenebralis.dreamos.presentation.screens.wallet

/**
 * 钱包页面事件
 */
sealed interface WalletEvent {
    data object Refresh : WalletEvent
    data class SwitchFilter(val filter: TransactionFilter) : WalletEvent
    data object DismissError : WalletEvent
}
