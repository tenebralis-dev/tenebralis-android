package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.mapper.toDto
import com.tenebralis.dreamos.data.remote.dto.CurrencyAccountDto
import com.tenebralis.dreamos.data.remote.dto.CurrencyTransactionDto
import com.tenebralis.dreamos.domain.model.CurrencyAccount
import com.tenebralis.dreamos.domain.model.CurrencyTransaction
import com.tenebralis.dreamos.domain.repository.CurrencyRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class CurrencyRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : CurrencyRepository {

    // ─── getOrCreate ─────────────────────────────────────────

    override suspend fun getOrCreateGlobalAccount(currencyCode: String): Result<CurrencyAccount> =
        runCatching {
            val userId = requireCurrentUserId()
            val existing = supabase.from(TABLE_ACCOUNTS)
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("scope_type", "global")
                        eq("currency_code", currencyCode)
                    }
                }
                .decodeList<CurrencyAccountDto>()
                .firstOrNull()

            if (existing != null) return@runCatching existing.toDomain()

            val newAccount = CurrencyAccount(
                id = UUID.randomUUID().toString(),
                userId = userId,
                scopeType = "global",
                worldId = null,
                currencyCode = currencyCode,
                balance = INITIAL_POINTS
            )
            supabase.from(TABLE_ACCOUNTS)
                .insert(newAccount.toDto()) { select() }
                .decodeSingle<CurrencyAccountDto>()
                .toDomain()
        }

    override suspend fun getOrCreateWorldAccount(
        worldId: String,
        currencyCode: String
    ): Result<CurrencyAccount> = runCatching {
        val userId = requireCurrentUserId()
        require(worldId.isNotBlank()) { "worldId 不能为空" }

        val existing = supabase.from(TABLE_ACCOUNTS)
            .select {
                filter {
                    eq("user_id", userId)
                    eq("scope_type", "world")
                    eq("world_id", worldId)
                    eq("currency_code", currencyCode)
                }
            }
            .decodeList<CurrencyAccountDto>()
            .firstOrNull()

        if (existing != null) return@runCatching existing.toDomain()

        val newAccount = CurrencyAccount(
            id = UUID.randomUUID().toString(),
            userId = userId,
            scopeType = "world",
            worldId = worldId,
            currencyCode = currencyCode,
            balance = INITIAL_COINS
        )
        supabase.from(TABLE_ACCOUNTS)
            .insert(newAccount.toDto()) { select() }
            .decodeSingle<CurrencyAccountDto>()
            .toDomain()
    }

    // ─── 余额 ────────────────────────────────────────────────

    override suspend fun getBalance(accountId: String): Result<Long> = runCatching {
        val userId = requireCurrentUserId()
        supabase.from(TABLE_ACCOUNTS)
            .select {
                filter {
                    eq("id", accountId)
                    eq("user_id", userId)
                }
            }
            .decodeSingle<CurrencyAccountDto>()
            .balance
    }

    // ─── 交易 ────────────────────────────────────────────────

    override suspend fun addTransaction(
        accountId: String,
        amount: Long,
        reasonType: String?,
        reasonRef: String?
    ): Result<CurrencyTransaction> = runCatching {
        val userId = requireCurrentUserId()

        // 1. 读取当前账户
        val account = supabase.from(TABLE_ACCOUNTS)
            .select {
                filter {
                    eq("id", accountId)
                    eq("user_id", userId)
                }
            }
            .decodeSingle<CurrencyAccountDto>()

        val newBalance = account.balance + amount
        require(newBalance >= 0) { "余额不足：当前 ${account.balance}，变动 $amount" }

        // 2. 更新余额
        supabase.from(TABLE_ACCOUNTS)
            .update({ set("balance", newBalance) }) {
                filter {
                    eq("id", accountId)
                    eq("user_id", userId)
                }
            }

        // 3. 写入交易流水
        val tx = CurrencyTransaction(
            id = UUID.randomUUID().toString(),
            userId = userId,
            accountId = accountId,
            worldId = account.worldId,
            amount = amount,
            reasonType = reasonType,
            reasonRef = reasonRef
        )
        supabase.from(TABLE_TRANSACTIONS)
            .insert(tx.toDto()) { select() }
            .decodeSingle<CurrencyTransactionDto>()
            .toDomain()
    }

    // ─── 列表查询 ────────────────────────────────────────────

    override fun getTransactions(accountId: String): Flow<Result<List<CurrencyTransaction>>> =
        flow {
            emit(runCatching {
                val userId = requireCurrentUserId()
                supabase.from(TABLE_TRANSACTIONS)
                    .select {
                        filter {
                            eq("account_id", accountId)
                            eq("user_id", userId)
                        }
                        order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    }
                    .decodeList<CurrencyTransactionDto>()
                    .map { it.toDomain() }
            })
        }.catch { emit(Result.failure(it)) }

    override suspend fun getAccountsByScope(scopeType: String): Result<List<CurrencyAccount>> =
        runCatching {
            val userId = requireCurrentUserId()
            supabase.from(TABLE_ACCOUNTS)
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("scope_type", scopeType)
                    }
                }
                .decodeList<CurrencyAccountDto>()
                .map { it.toDomain() }
        }

    // ─── 内部 ────────────────────────────────────────────────

    private fun requireCurrentUserId(): String =
        supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")

    private companion object {
        const val TABLE_ACCOUNTS = "currency_accounts"
        const val TABLE_TRANSACTIONS = "currency_transactions"
        const val INITIAL_POINTS = 100L
        const val INITIAL_COINS = 500L
    }
}
