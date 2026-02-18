package com.tenebralis.dreamos.presentation.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    fun onEvent(event: WalletEvent) {
        when (event) {
            WalletEvent.Refresh -> loadAccounts()
            is WalletEvent.SwitchFilter -> {
                _uiState.update { it.copy(selectedFilter = event.filter) }
                loadTransactions()
            }
            WalletEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 加载全局积分账户
            currencyRepository.getOrCreateGlobalAccount()
                .onSuccess { account ->
                    _uiState.update { it.copy(globalAccount = account) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "加载账户失败") }
                }

            // 加载世界货币账户列表
            currencyRepository.getAccountsByScope("world")
                .onSuccess { accounts ->
                    _uiState.update { it.copy(worldAccounts = accounts) }
                }

            _uiState.update { it.copy(isLoading = false) }
            loadTransactions()
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            val globalAccount = _uiState.value.globalAccount ?: return@launch

            currencyRepository.getTransactions(globalAccount.id).collect { result ->
                result.onSuccess { transactions ->
                    val filter = _uiState.value.selectedFilter
                    val filtered = if (filter.reasonType == null) {
                        transactions
                    } else {
                        transactions.filter { it.reasonType == filter.reasonType }
                    }
                    _uiState.update { it.copy(transactions = filtered) }
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "加载流水失败") }
                }
            }
        }
    }
}
