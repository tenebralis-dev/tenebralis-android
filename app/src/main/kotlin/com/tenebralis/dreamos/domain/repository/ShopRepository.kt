package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.InventoryItem
import com.tenebralis.dreamos.domain.model.ShopItem
import kotlinx.coroutines.flow.Flow

/**
 * 商店仓库接口
 *
 * 对应表：shop_items + user_inventory
 */
interface ShopRepository {

    /** 获取上架商品列表 */
    fun getItems(worldId: String? = null): Flow<Result<List<ShopItem>>>

    /** 获取单件商品 */
    suspend fun getItemById(itemId: String): Result<ShopItem>

    /** 获取用户背包 */
    fun getInventory(): Flow<Result<List<InventoryItem>>>

    /** 扣减库存（stock -= 1，若为 null 则忽略） */
    suspend fun decrementStock(itemId: String): Result<Unit>

    /** 增加背包物品（upsert quantity += 1） */
    suspend fun addToInventory(itemId: String): Result<InventoryItem>
}
