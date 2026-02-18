package com.tenebralis.dreamos.presentation.screens.shop

import com.tenebralis.dreamos.domain.model.ShopItem

/**
 * 商店页面事件
 */
sealed interface ShopEvent {
    data object Refresh : ShopEvent
    data class SwitchTab(val tab: ShopTab) : ShopEvent
    data class ShowPurchaseDialog(val item: ShopItem) : ShopEvent
    data object DismissPurchaseDialog : ShopEvent
    data class ConfirmPurchase(val itemId: String) : ShopEvent
    data object DismissError : ShopEvent
    data object DismissSuccess : ShopEvent
}
