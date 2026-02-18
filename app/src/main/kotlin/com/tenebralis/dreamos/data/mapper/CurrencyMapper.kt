package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.CurrencyAccountDto
import com.tenebralis.dreamos.data.remote.dto.CurrencyTransactionDto
import com.tenebralis.dreamos.domain.model.CurrencyAccount
import com.tenebralis.dreamos.domain.model.CurrencyTransaction

fun CurrencyAccountDto.toDomain() = CurrencyAccount(
    id = id,
    userId = userId,
    scopeType = scopeType,
    worldId = worldId,
    currencyCode = currencyCode,
    balance = balance,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CurrencyAccount.toDto() = CurrencyAccountDto(
    id = id,
    userId = userId,
    scopeType = scopeType,
    worldId = worldId,
    currencyCode = currencyCode,
    balance = balance,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CurrencyTransactionDto.toDomain() = CurrencyTransaction(
    id = id,
    userId = userId,
    accountId = accountId,
    worldId = worldId,
    amount = amount,
    reasonType = reasonType,
    reasonRef = reasonRef,
    metadataJson = metadataJson,
    createdAt = createdAt
)

fun CurrencyTransaction.toDto() = CurrencyTransactionDto(
    id = id,
    userId = userId,
    accountId = accountId,
    worldId = worldId,
    amount = amount,
    reasonType = reasonType,
    reasonRef = reasonRef,
    metadataJson = metadataJson,
    createdAt = createdAt
)
