package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.ApiConnectionDto
import com.tenebralis.dreamos.domain.model.ApiConnection

fun ApiConnectionDto.toDomain() = ApiConnection(
    id = id,
    userId = userId,
    name = name,
    serviceType = serviceType,
    baseUrl = baseUrl,
    isSynced = isSynced,
    isActive = isActive,
    defaultModel = defaultModel,
    systemPrompt = systemPrompt,
    paramsJson = paramsJson,
    headersTemplateJson = headersTemplateJson,
    configJson = configJson,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ApiConnection.toDto() = ApiConnectionDto(
    id = id,
    userId = userId,
    name = name,
    serviceType = serviceType,
    baseUrl = baseUrl,
    isSynced = isSynced,
    isActive = isActive,
    defaultModel = defaultModel,
    systemPrompt = systemPrompt,
    paramsJson = paramsJson,
    headersTemplateJson = headersTemplateJson,
    configJson = configJson,
    createdAt = createdAt,
    updatedAt = updatedAt
)
