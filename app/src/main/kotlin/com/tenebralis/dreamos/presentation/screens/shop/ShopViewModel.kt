package com.tenebralis.dreamos.presentation.screens.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebralis.dreamos.domain.repository.ShopRepository
import com.tenebralis.dreamos.domain.usecase.shop.PurchaseItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shopRepository: ShopRepository,
    private val purchaseItemUseCase: PurchaseItemUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    init {
        loadShopItems()
    }

    fun onEvent(event: ShopEvent) {
        when (event) {
            ShopEvent.Refresh -> {
                loadShopItems()
                loadInventory()
            }
            is ShopEvent.SwitchTab -> {
                _uiState.update { it.copy(selectedTab = event.tab) }
                if (event.tab == ShopTab.INVENTORY) loadInventory()
            }
            is ShopEvent.ShowPurchaseDialog -> {
                _uiState.update { it.copy(showPurchaseDialog = event.item) }
            }
            ShopEvent.DismissPurchaseDialog -> {
                _uiState.update { it.copy(showPurchaseDialog = null) }
            }
            is ShopEvent.ConfirmPurchase -> purchase(event.itemId)
            ShopEvent.DismissError -> _uiState.update { it.copy(error = null) }
            ShopEvent.DismissSuccess -> _uiState.update { it.copy(purchaseSuccess = null) }
        }
    }

    private fun loadShopItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            shopRepository.getItems().collect { result ->
                result.onSuccess { items ->
                    _uiState.update { it.copy(shopItems = items, isLoading = false) }
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(error = e.message ?: "加载商品失败", isLoading = false)
                    }
                }
            }
        }
    }

    private fun loadInventory() {
        viewModelScope.launch {
            shopRepository.getInventory().collect { result ->
                result.onSuccess { items ->
                    _uiState.update { it.copy(inventoryItems = items) }
                }
            }
        }
    }

    private fun purchase(itemId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showPurchaseDialog = null) }
            purchaseItemUseCase(itemId)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, purchaseSuccess = "购买成功！")
                    }
                    loadShopItems()
                    loadInventory()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "购买失败"
                        )
                    }
                }
        }
    }
}
