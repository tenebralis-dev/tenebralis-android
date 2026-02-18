package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.remote.dto.InventoryItemDto
import com.tenebralis.dreamos.data.remote.dto.ShopItemDto
import com.tenebralis.dreamos.domain.model.InventoryItem
import com.tenebralis.dreamos.domain.model.ShopItem
import com.tenebralis.dreamos.domain.repository.ShopRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class ShopRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : ShopRepository {

    // ─── 商品 ─────────────────────────────────────────────

    override fun getItems(worldId: String?): Flow<Result<List<ShopItem>>> =
        flow {
            emit(runCatching {
                val userId = requireCurrentUserId()
                supabase.from(TABLE_ITEMS)
                    .select {
                        filter {
                            eq("user_id", userId)
                            eq("is_active", true)
                            if (worldId != null) {
                                eq("world_id", worldId)
                            }
                        }
                    }
                    .decodeList<ShopItemDto>()
                    .map { it.toDomain() }
            })
        }.catch { emit(Result.failure(it)) }

    override suspend fun getItemById(itemId: String): Result<ShopItem> = runCatching {
        supabase.from(TABLE_ITEMS)
            .select { filter { eq("id", itemId) } }
            .decodeSingle<ShopItemDto>()
            .toDomain()
    }

    // ─── 背包 ─────────────────────────────────────────────

    override fun getInventory(): Flow<Result<List<InventoryItem>>> =
        flow {
            emit(runCatching {
                val userId = requireCurrentUserId()
                supabase.from(TABLE_INVENTORY)
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<InventoryItemDto>()
                    .map { it.toDomain() }
            })
        }.catch { emit(Result.failure(it)) }

    override suspend fun decrementStock(itemId: String): Result<Unit> = runCatching {
        val item = getItemById(itemId).getOrThrow()
        val stock = item.stock ?: return@runCatching  // null = 无限库存
        if (stock <= 0) throw IllegalStateException("库存不足")
        supabase.from(TABLE_ITEMS)
            .update({ set("stock", stock - 1) }) {
                filter { eq("id", itemId) }
            }
    }

    override suspend fun addToInventory(itemId: String): Result<InventoryItem> = runCatching {
        val userId = requireCurrentUserId()

        // 尝试查找已有记录
        val existingList = supabase.from(TABLE_INVENTORY)
            .select {
                filter {
                    eq("user_id", userId)
                    eq("item_id", itemId)
                }
            }
            .decodeList<InventoryItemDto>()

        if (existingList.isNotEmpty()) {
            val existing = existingList.first()
            supabase.from(TABLE_INVENTORY)
                .update({ set("quantity", existing.quantity + 1) }) {
                    filter { eq("id", existing.id) }
                }
            existing.copy(quantity = existing.quantity + 1).toDomain()
        } else {
            val newItem = InventoryItemDto(
                id = UUID.randomUUID().toString(),
                userId = userId,
                itemId = itemId,
                quantity = 1
            )
            supabase.from(TABLE_INVENTORY)
                .insert(newItem) { select() }
                .decodeSingle<InventoryItemDto>()
                .toDomain()
        }
    }

    // ─── 内部 ─────────────────────────────────────────────

    private fun requireCurrentUserId(): String =
        supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")

    private companion object {
        const val TABLE_ITEMS = "shop_items"
        const val TABLE_INVENTORY = "user_inventory"
    }
}
