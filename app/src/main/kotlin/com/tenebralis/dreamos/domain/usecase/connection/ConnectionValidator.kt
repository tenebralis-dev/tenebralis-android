package com.tenebralis.dreamos.domain.usecase.connection

import com.tenebralis.dreamos.domain.model.ApiConnection
import java.net.URI
import javax.inject.Inject
import kotlinx.serialization.json.JsonObject

class ConnectionValidator @Inject constructor() {

    fun validate(
        name: String,
        serviceType: String,
        baseUrl: String,
        headersTemplateJson: JsonObject
    ): Result<Unit> = runCatching {
        require(name.trim().isNotEmpty()) { "连接名称不能为空" }
        require(serviceType.trim().isNotEmpty()) { "服务类型不能为空" }
        validateBaseUrl(baseUrl)
        validateHeadersTemplateJson(headersTemplateJson)
    }

    fun validate(connection: ApiConnection): Result<Unit> = validate(
        name = connection.name,
        serviceType = connection.serviceType,
        baseUrl = connection.baseUrl,
        headersTemplateJson = connection.headersTemplateJson
    )

    private fun validateBaseUrl(baseUrl: String) {
        val normalized = baseUrl.trim()
        val uri = runCatching { URI(normalized) }.getOrNull()
        val scheme = uri?.scheme?.lowercase()
        require(scheme == "http" || scheme == "https") { "Base URL 必须是 http/https" }
        require(!uri.host.isNullOrBlank()) { "Base URL 缺少主机名" }
    }

    private fun validateHeadersTemplateJson(headersTemplateJson: JsonObject) {
        val forbiddenHeaders = setOf("authorization", "x-api-key", "api-key")
        headersTemplateJson.keys.forEach { key ->
            val normalized = key.trim().lowercase()
            val normalizedDash = normalized.replace("_", "-")
            require(normalized !in forbiddenHeaders && normalizedDash !in forbiddenHeaders) {
                "headers_template_json 禁止包含密钥头：$key"
            }
        }
    }
}
