package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * api_connections 表 DTO
 *
 * headers_template_json 禁止包含密钥头（PRD §5.4），
 * 由 SQL CHECK 约束 + Repository 写入前校验双重保证。
 */
@Serializable
data class ApiConnectionDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val name: String,
    @SerialName("service_type") val serviceType: String = "openai_compat",
    @SerialName("base_url") val baseUrl: String,
    @SerialName("is_synced") val isSynced: Boolean = true,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("default_model") val defaultModel: String? = null,
    @SerialName("system_prompt") val systemPrompt: String? = null,
    @SerialName("params_json") val paramsJson: JsonObject = JsonObject(emptyMap()),
    @SerialName("headers_template_json") val headersTemplateJson: JsonObject = JsonObject(emptyMap()),
    @SerialName("config_json") val configJson: JsonObject = JsonObject(emptyMap()),
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
