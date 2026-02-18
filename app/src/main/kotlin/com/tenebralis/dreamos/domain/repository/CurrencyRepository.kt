package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.CurrencyAccount
import com.tenebralis.dreamos.domain.model.CurrencyTransaction
import kotlinx.coroutines.flow.Flow

/**
 * 货币仓库接口
 *
 * 对应表：currency_accounts + currency_transactions
 */
interface CurrencyRepository {

    /** 获取或创建全局积分账户（初始 100） */
    suspend fun getOrCreateGlobalAccount(currencyCode: String = "points"): Result<CurrencyAccount>

    /** 获取或创建世界货币账户（初始 500） */
    suspend fun getOrCreateWorldAccount(worldId: String, currencyCode: String = "coins"): Result<CurrencyAccount>

    /** 查询账户余额 */
    suspend fun getBalance(accountId: String): Result<Long>

    /** 新增交易流水（同时更新余额） */
    suspend fun addTransaction(
        accountId: String,
        amount: Long,
        reasonType: String?,
        reasonRef: String?
    ): Result<CurrencyTransaction>

    /** 获取某账户的交易流水 */
    fun getTransactions(accountId: String): Flow<Result<List<CurrencyTransaction>>>

    /** 按作用域获取账户列表 */
    suspend fun getAccountsByScope(scopeType: String): Result<List<CurrencyAccount>>
}
