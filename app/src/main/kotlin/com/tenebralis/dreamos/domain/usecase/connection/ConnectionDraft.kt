package com.tenebralis.dreamos.domain.usecase.connection

import com.tenebralis.dreamos.domain.model.ConnectionConfig
import kotlinx.serialization.json.JsonObject

data class ConnectionDraft(
    val name: String,
    val serviceType: String,
    val baseUrl: String,
    val defaultModel: String?,
    val systemPrompt: String?,
    val paramsJson: JsonObject,
    val headersTemplateJson: JsonObject,
    val config: ConnectionConfig = ConnectionConfig()
) {
    /** 序列化 config 为 JsonObject，供 Repository 使用 */
    fun toConfigJsonObject(): JsonObject = config.toJsonObject()
}
