package com.tenebralis.dreamos.domain.usecase.shop

import com.tenebralis.dreamos.domain.model.InventoryItem
import com.tenebralis.dreamos.domain.repository.CurrencyRepository
import com.tenebralis.dreamos.domain.repository.ShopRepository
import javax.inject.Inject

/**
 * 购买商品用例
 *
 * 流程：查价格 → 查余额 → 扣款 → 扣库存 → 入库
 */
class PurchaseItemUseCase @Inject constructor(
    private val shopRepository: ShopRepository,
    private val currencyRepository: CurrencyRepository
) {
    suspend operator fun invoke(itemId: String): Result<InventoryItem> = runCatching {
        // 1. 查商品
        val item = shopRepository.getItemById(itemId).getOrThrow()
        require(item.isActive) { "商品已下架" }
        item.stock?.let { require(it > 0) { "库存不足" } }

        // 2. 获取账户（根据 currencyCode 决定全局 or 世界账户）
        val account = if (item.worldId != null) {
            currencyRepository.getOrCreateWorldAccount(item.worldId)
        } else {
            currencyRepository.getOrCreateGlobalAccount()
        }.getOrThrow()

        // 3. 扣款（addTransaction 内部校验余额）
        currencyRepository.addTransaction(
            accountId = account.id,
            amount = -item.price,
            reasonType = "purchase",
            reasonRef = item.id
        ).getOrThrow()

        // 4. 扣库存
        shopRepository.decrementStock(itemId).getOrThrow()

        // 5. 入库
        shopRepository.addToInventory(itemId).getOrThrow()
    }
}
