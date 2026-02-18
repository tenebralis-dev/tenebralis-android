package com.tenebralis.dreamos.presentation.screens.shop

import com.tenebralis.dreamos.domain.model.InventoryItem
import com.tenebralis.dreamos.domain.model.ShopItem

/**
 * 商店页面 UI 状态
 */
data class ShopUiState(
    val shopItems: List<ShopItem> = emptyList(),
    val inventoryItems: List<InventoryItem> = emptyList(),
    val selectedTab: ShopTab = ShopTab.SHOP,
    val isLoading: Boolean = false,
    val error: String? = null,
    val purchaseSuccess: String? = null,
    val showPurchaseDialog: ShopItem? = null
)

enum class ShopTab(val displayName: String) {
    SHOP("商店"),
    INVENTORY("背包")
}
