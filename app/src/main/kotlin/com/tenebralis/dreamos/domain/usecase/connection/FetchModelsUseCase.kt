package com.tenebralis.dreamos.domain.usecase.connection

import com.tenebralis.dreamos.domain.repository.ConnectionSecretRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * 从 OpenAI 兼容的 /models 端点拉取可用模型列表。
 *
 * 返回按字母排序的模型 ID 列表。
 */
class FetchModelsUseCase @Inject constructor(
    private val httpClient: HttpClient,
    private val connectionSecretRepository: ConnectionSecretRepository
) {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 拉取模型列表。
     *
     * @param baseUrl  API Base URL
     * @param apiKey   用户输入的 API Key（优先），为空则尝试已保存的 Key
     * @param connectionId  已保存连接的 ID（用于获取已存的 Key），可为 null
     * @param headersTemplateJson  自定义 headers 模板
     */
    suspend operator fun invoke(
        baseUrl: String,
        apiKey: String,
        connectionId: String?,
        headersTemplateJson: JsonObject
    ): Result<List<String>> = runCatching {
        require(baseUrl.isNotBlank()) { "Base URL 不能为空" }

        val resolvedKey = resolveApiKey(apiKey, connectionId)
        val endpoint = "${baseUrl.trimEnd('/')}/models"

        val response = httpClient.get(endpoint) {
            header(HttpHeaders.Authorization, "Bearer $resolvedKey")
            header(HttpHeaders.Accept, "application/json")
            appendTemplateHeaders(headersTemplateJson)
        }

        if (!response.status.isSuccess()) {
            throw IllegalStateException("拉取模型失败（HTTP ${response.status.value}）")
        }

        val body = response.bodyAsText()
        parseModelIds(body)
    }

    private suspend fun resolveApiKey(apiKey: String, connectionId: String?): String {
        val inlineKey = apiKey.trim()
        if (inlineKey.isNotEmpty()) return inlineKey

        if (connectionId != null) {
            val savedKey = connectionSecretRepository.getSecret(connectionId)
                .getOrNull()
                ?.trim()
                .orEmpty()
            if (savedKey.isNotEmpty()) return savedKey
        }

        throw IllegalArgumentException("请先输入 API Key")
    }

    private fun parseModelIds(responseBody: String): List<String> {
        val parsed = json.parseToJsonElement(responseBody)
        val dataArray = parsed.jsonObject["data"]?.jsonArray
            ?: throw IllegalStateException("响应缺少 data 数组")

        return dataArray.mapNotNull { element ->
            element.jsonObject["id"]?.jsonPrimitive?.contentOrNull
        }.filter { it.isNotBlank() }.sorted()
    }

    private fun io.ktor.client.request.HttpRequestBuilder.appendTemplateHeaders(
        headersTemplateJson: JsonObject
    ) {
        val forbidden = setOf("authorization", "x-api-key", "api-key", "content-type")
        headersTemplateJson.forEach { (key, value) ->
            val normalized = key.trim().lowercase().replace("_", "-")
            if (normalized in forbidden) return@forEach

            val headerValue = when {
                value is JsonPrimitive && value.isString -> value.content
                else -> value.toString()
            }
            if (headerValue.isNotBlank()) {
                header(key, headerValue)
            }
        }
    }
}
