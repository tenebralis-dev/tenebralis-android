package com.tenebralis.dreamos.domain.model

import kotlinx.serialization.json.JsonObject

/**
 * API 连接配置（领域模型）
 *
 * 对应表：api_connections
 */
data class ApiConnection(
    val id: String,
    val userId: String,
    val name: String,
    val serviceType: String,
    val baseUrl: String,
    val isSynced: Boolean,
    val isActive: Boolean,
    val defaultModel: String?,
    val systemPrompt: String?,
    val paramsJson: JsonObject,
    val headersTemplateJson: JsonObject,
    val configJson: JsonObject,
    val createdAt: String?,
    val updatedAt: String?
)
