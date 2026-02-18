package com.tenebralis.dreamos.data.remote.ai

import com.tenebralis.dreamos.di.AiHttpClient
import com.tenebralis.dreamos.domain.model.ApiConnection
import com.tenebralis.dreamos.domain.service.AiChatService
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.readUTF8Line
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * OpenAI 兼容 Chat Completions 调用实现。
 *
 * 使用专用 [AiHttpClient] 限定的长超时 HttpClient。
 */
class AiChatServiceImpl @Inject constructor(
    @AiHttpClient private val httpClient: HttpClient
) : AiChatService {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun chatCompletion(
        connection: ApiConnection,
        apiKey: String,
        messages: List<ChatMessage>
    ): Result<ChatCompletionResponse> = runCatching {
        require(connection.baseUrl.isNotBlank()) { "Base URL 不能为空" }
        require(apiKey.isNotBlank()) { "API Key 不能为空" }
        require(messages.isNotEmpty()) { "消息列表不能为空" }

        val model = connection.defaultModel?.takeIf { it.isNotBlank() }
            ?: DEFAULT_MODEL
        val request = buildRequest(model, messages, connection.paramsJson, stream = false)
        val endpoint = "${connection.baseUrl.trimEnd('/')}/chat/completions"

        try {
            val response = httpClient.post(endpoint) {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                header(HttpHeaders.Accept, "application/json")
                appendTemplateHeaders(connection.headersTemplateJson)
                setBody(json.encodeToString(ChatCompletionRequest.serializer(), request))
            }

            val statusCode = response.status.value
            val responseBody = response.bodyAsText()

            if (response.status.isSuccess()) {
                val parsed = json.decodeFromString(
                    ChatCompletionResponse.serializer(),
                    responseBody
                )
                require(parsed.choices.isNotEmpty()) {
                    "AI 返回了空的 choices 列表"
                }
                parsed
            } else {
                throw AiApiException(
                    statusCode = statusCode,
                    message = mapHttpError(statusCode, responseBody)
                )
            }
        } catch (e: AiApiException) {
            throw e
        } catch (_: HttpRequestTimeoutException) {
            throw AiApiException(
                statusCode = null,
                message = "AI 响应超时，请稍后重试"
            )
        } catch (e: Throwable) {
            throw AiApiException(
                statusCode = null,
                message = "AI 调用失败：${e.localizedMessage ?: "未知错误"}"
            )
        }
    }

    override fun chatCompletionStream(
        connection: ApiConnection,
        apiKey: String,
        messages: List<ChatMessage>
    ): Flow<Result<String>> = flow {
        require(connection.baseUrl.isNotBlank()) { "Base URL 不能为空" }
        require(apiKey.isNotBlank()) { "API Key 不能为空" }
        require(messages.isNotEmpty()) { "消息列表不能为空" }

        val model = connection.defaultModel?.takeIf { it.isNotBlank() }
            ?: DEFAULT_MODEL
        val request = buildRequest(model, messages, connection.paramsJson, stream = true)
        val endpoint = "${connection.baseUrl.trimEnd('/')}/chat/completions"

        try {
            httpClient.preparePost(endpoint) {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                header(HttpHeaders.Accept, "text/event-stream")
                appendTemplateHeaders(connection.headersTemplateJson)
                setBody(json.encodeToString(ChatCompletionRequest.serializer(), request))
            }.execute { response ->
                if (!response.status.isSuccess()) {
                    val responseBody = response.bodyAsText()
                    emit(Result.failure(
                        AiApiException(
                            statusCode = response.status.value,
                            message = mapHttpError(response.status.value, responseBody)
                        )
                    ))
                    return@execute
                }

                val channel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: break

                    // SSE 协议：空行为事件分隔符，直接跳过
                    if (line.isBlank()) continue

                    // 仅处理 data: 开头的行
                    if (!line.startsWith("data:")) continue

                    val payload = line.removePrefix("data:").trim()

                    // [DONE] 信号：流结束
                    if (payload == "[DONE]") break

                    // 解析 chunk
                    val content = parseSseChunkContent(payload)
                    if (content != null) {
                        emit(Result.success(content))
                    }
                }
            }
        } catch (_: HttpRequestTimeoutException) {
            emit(Result.failure(
                AiApiException(statusCode = null, message = "AI 响应超时，请稍后重试")
            ))
        } catch (e: AiApiException) {
            emit(Result.failure(e))
        } catch (e: Throwable) {
            emit(Result.failure(
                AiApiException(
                    statusCode = null,
                    message = "AI 流式调用失败：${e.localizedMessage ?: "未知错误"}"
                )
            ))
        }
    }

    // ── 内部辅助方法 ──

    /**
     * 从 SSE data 行的 JSON payload 中提取 delta.content。
     *
     * @return delta content（可能为 null，例如首个 chunk 只有 role）
     */
    internal fun parseSseChunkContent(payload: String): String? {
        return runCatching {
            val chunk = json.decodeFromString(
                ChatCompletionStreamChunk.serializer(),
                payload
            )
            chunk.choices.firstOrNull()?.delta?.content
        }.getOrNull()
    }

    private fun buildRequest(
        model: String,
        messages: List<ChatMessage>,
        paramsJson: JsonObject,
        stream: Boolean
    ): ChatCompletionRequest {
        return ChatCompletionRequest(
            model = model,
            messages = messages,
            temperature = paramsJson.doubleParam("temperature") ?: 0.7,
            maxTokens = paramsJson.intParam("max_tokens"),
            topP = paramsJson.doubleParam("top_p"),
            frequencyPenalty = paramsJson.doubleParam("frequency_penalty"),
            presencePenalty = paramsJson.doubleParam("presence_penalty"),
            stream = stream
        )
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

    private fun mapHttpError(statusCode: Int, responseBody: String): String {
        val providerMessage = extractProviderMessage(responseBody)
        return when (statusCode) {
            400 -> "请求参数错误（400）${providerMessage?.let { "：$it" } ?: "，请检查模型名称与参数"}"
            401 -> "认证失败（401），请检查 API Key${providerMessage?.let { "：$it" } ?: ""}"
            403 -> "无权限访问（403）${providerMessage?.let { "：$it" } ?: ""}"
            404 -> "接口不存在（404），请确认 Base URL 是否正确"
            429 -> "请求过于频繁（429），请稍后重试${providerMessage?.let { "：$it" } ?: ""}"
            500, 502, 503 -> "AI 服务暂时不可用（$statusCode），请稍后重试"
            else -> "AI 调用失败（HTTP $statusCode）${providerMessage?.let { "：$it" } ?: ""}"
        }
    }

    private fun extractProviderMessage(responseBody: String): String? {
        val parsed = runCatching {
            json.parseToJsonElement(responseBody)
        }.getOrNull() ?: return null

        val root = parsed.jsonObject
        val errorNode = root["error"] ?: return null
        return when (errorNode) {
            is JsonObject -> errorNode["message"]?.jsonPrimitive?.contentOrNull
            else -> errorNode.toString()
        }?.takeIf { it.isNotBlank() }
    }

    private fun JsonObject.doubleParam(key: String): Double? {
        return this[key]?.jsonPrimitive?.doubleOrNull
    }

    private fun JsonObject.intParam(key: String): Int? {
        return this[key]?.jsonPrimitive?.intOrNull
    }

    private companion object {
        const val DEFAULT_MODEL = "gpt-3.5-turbo"
    }
}

/**
 * AI API 调用异常。
 *
 * @param statusCode HTTP 状态码（超时等场景为 null）
 * @param message 面向用户的中文错误提示
 */
class AiApiException(
    val statusCode: Int?,
    override val message: String
) : Exception(message)
