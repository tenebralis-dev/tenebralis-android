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
import java.net.UnknownHostException
import java.net.ConnectException
import io.ktor.client.plugins.HttpRequestTimeoutException

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

        try {
            val response = httpClient.get(endpoint) {
                header(HttpHeaders.Authorization, "Bearer $resolvedKey")
                header(HttpHeaders.Accept, "application/json")
                appendTemplateHeaders(headersTemplateJson)
            }

            val body = response.bodyAsText()

            if (!response.status.isSuccess()) {
                val providerError = extractProviderMessage(body)
                val status = response.status.value
                val message = when (status) {
                    401 -> "认证失败 (401)，请检查 API Key${providerError?.let { "：$it" } ?: ""}"
                    403 -> "无权限访问 (403)${providerError?.let { "：$it" } ?: ""}"
                    404 -> "模型列表端点不存在 (404)，请确认 Base URL 是否正确"
                    else -> "拉取模型失败 (HTTP $status)${providerError?.let { "：$it" } ?: ""}"
                }
                throw IllegalStateException(message)
            }

            parseModelIds(body)
        } catch (e: Exception) {
            when (e) {
                is UnknownHostException -> throw IllegalStateException("无法解析域名，请检查网络或 Base URL 拼写：${e.localizedMessage}")
                is ConnectException -> throw IllegalStateException("连接服务器失败，请检查网络地址：${e.localizedMessage}")
                is HttpRequestTimeoutException -> throw IllegalStateException("请求超时，网络状况可能不佳，请重试")
                else -> throw e
            }
        }
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

    private fun extractProviderMessage(responseBody: String): String? {
        val parsed = runCatching { json.parseToJsonElement(responseBody) }.getOrNull() ?: return null
        val root = parsed.jsonObject
        val errorNode = root["error"] ?: return null
        return when (errorNode) {
            is JsonObject -> errorNode["message"]?.jsonPrimitive?.contentOrNull
            else -> errorNode.toString()
        }?.takeIf { it.isNotBlank() }
    }
}
