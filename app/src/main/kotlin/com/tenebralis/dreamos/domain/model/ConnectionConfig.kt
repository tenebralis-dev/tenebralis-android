package com.tenebralis.dreamos.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * 连接级配置（结构化 config_json）。
 *
 * 序列化后存储在 api_connections.config_json JSONB 字段中。
 * 使用 [toJsonObject] / [fromJsonObject] 与原有 JsonObject 互转。
 */
@Serializable
data class ConnectionConfig(
    @SerialName("stream_enabled") val streamEnabled: Boolean = true,
    @SerialName("timeout_seconds") val timeoutSeconds: Int = 120,
    @SerialName("retry_count") val retryCount: Int = 3
) {
    fun toJsonObject(): JsonObject {
        return configJson.encodeToJsonElement(this) as JsonObject
    }

    companion object {
        private val configJson = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        fun fromJsonObject(json: JsonObject): ConnectionConfig {
            return if (json.isEmpty()) {
                ConnectionConfig()
            } else {
                runCatching {
                    configJson.decodeFromJsonElement<ConnectionConfig>(json)
                }.getOrDefault(ConnectionConfig())
            }
        }
    }
}
