package com.tenebralis.dreamos.presentation.screens.wallet

import com.tenebralis.dreamos.domain.model.CurrencyAccount
import com.tenebralis.dreamos.domain.model.CurrencyTransaction

/**
 * 钱包页面 UI 状态
 */
data class WalletUiState(
    val globalAccount: CurrencyAccount? = null,
    val worldAccounts: List<CurrencyAccount> = emptyList(),
    val transactions: List<CurrencyTransaction> = emptyList(),
    val selectedFilter: TransactionFilter = TransactionFilter.ALL,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class TransactionFilter(val displayName: String, val reasonType: String?) {
    ALL("全部", null),
    TASK_REWARD("任务奖励", "task_reward"),
    PURCHASE("购买", "purchase"),
    POMODORO("番茄钟", "pomodoro")
}
