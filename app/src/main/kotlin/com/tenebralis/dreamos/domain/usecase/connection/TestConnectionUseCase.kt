package com.tenebralis.dreamos.domain.usecase.connection

import com.tenebralis.dreamos.domain.model.ApiConnection
import com.tenebralis.dreamos.domain.repository.ConnectionSecretRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class TestConnectionUseCase @Inject constructor(
    private val httpClient: HttpClient,
    private val connectionSecretRepository: ConnectionSecretRepository
) {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend operator fun invoke(
        connection: ApiConnection,
        apiKeyOverride: String? = null
    ): Result<ConnectionTestResult> = runCatching {
        require(connection.baseUrl.isNotBlank()) { "Base URL 不能为空" }
        require(connection.serviceType.trim().isNotBlank()) { "serviceType 不能为空" }

        val apiKey = resolveApiKey(connection.id, apiKeyOverride)
        val endpoint = "${connection.baseUrl.trimEnd('/')}/models"
        val startNanos = System.nanoTime()

        try {
            val response = httpClient.get(endpoint) {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                header(HttpHeaders.Accept, "application/json")
                appendTemplateHeaders(connection.headersTemplateJson)
            }

            val elapsedMs = elapsedMs(startNanos)
            val statusCode = response.status.value
            val responseBody = response.bodyAsText()

            if (response.status.isSuccess()) {
                ensureModelsResponseShape(responseBody)
                ConnectionTestResult(
                    success = true,
                    statusCode = statusCode,
                    elapsedMs = elapsedMs,
                    message = "连接可用（GET /models）"
                )
            } else {
                ConnectionTestResult(
                    success = false,
                    statusCode = statusCode,
                    elapsedMs = elapsedMs,
                    message = mapHttpError(statusCode, responseBody)
                )
            }
        } catch (_: HttpRequestTimeoutException) {
            ConnectionTestResult(
                success = false,
                statusCode = null,
                elapsedMs = elapsedMs(startNanos),
                message = "连接超时，请检查 Base URL 或网络"
            )
        } catch (error: Throwable) {
            ConnectionTestResult(
                success = false,
                statusCode = null,
                elapsedMs = elapsedMs(startNanos),
                message = "连接失败：${error.localizedMessage ?: "未知错误"}"
            )
        }
    }

    private suspend fun resolveApiKey(connectionId: String, apiKeyOverride: String?): String {
        val inlineKey = apiKeyOverride?.trim().orEmpty()
        if (inlineKey.isNotEmpty()) return inlineKey

        val savedKey = connectionSecretRepository.getSecret(connectionId)
            .getOrThrow()
            ?.trim()
            .orEmpty()

        require(savedKey.isNotEmpty()) { "请先保存 API Key" }
        return savedKey
    }

    private fun HttpRequestBuilder.appendTemplateHeaders(headersTemplateJson: JsonObject) {
        val forbidden = setOf("authorization", "x-api-key", "api-key")
        headersTemplateJson.forEach { (key, value) ->
            val normalized = key.trim().lowercase().replace("_", "-")
            if (normalized in forbidden) return@forEach

            val headerValue = when {
                value.isStringPrimitive() -> value.jsonPrimitive.content
                else -> value.toString()
            }

            if (headerValue.isNotBlank()) {
                header(key, headerValue)
            }
        }
    }

    private fun ensureModelsResponseShape(responseBody: String) {
        val parsed = runCatching { json.parseToJsonElement(responseBody) }.getOrElse {
            throw IllegalStateException("响应解析失败：返回内容不是合法 JSON")
        }
        parsed.jsonObject["data"]?.jsonArray
            ?: throw IllegalStateException("响应解析失败：缺少 data 数组")
    }

    private fun mapHttpError(statusCode: Int, responseBody: String): String {
        val providerMessage = extractProviderMessage(responseBody)
        return when (statusCode) {
            401 -> "认证失败（401），请检查 API Key${providerMessage?.let { "：$it" } ?: ""}"
            403 -> "无权限访问（403）${providerMessage?.let { "：$it" } ?: ""}"
            404 -> "接口不存在（404），请确认 Base URL 是否为 OpenAI 兼容地址"
            else -> "连接失败（HTTP $statusCode）${providerMessage?.let { "：$it" } ?: ""}"
        }
    }

    private fun extractProviderMessage(responseBody: String): String? {
        val parsed = runCatching { json.parseToJsonElement(responseBody) }.getOrNull() ?: return null
        val root = parsed.jsonObject
        val errorNode = root["error"] ?: return null
        return when {
            errorNode is JsonObject -> errorNode["message"]?.jsonPrimitive?.contentOrNull
            else -> errorNode.toString()
        }?.takeIf { it.isNotBlank() }
    }

    private fun elapsedMs(startNanos: Long): Long {
        return (System.nanoTime() - startNanos) / 1_000_000L
    }
}

private fun kotlinx.serialization.json.JsonElement.isStringPrimitive(): Boolean {
    val primitive = this as? kotlinx.serialization.json.JsonPrimitive ?: return false
    return primitive.isString
}
