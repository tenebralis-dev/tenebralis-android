package com.tenebralis.dreamos.domain.usecase.chat

import android.util.Log
import com.tenebralis.dreamos.domain.model.ConversationMessage
import com.tenebralis.dreamos.domain.model.enums.MessageRole
import com.tenebralis.dreamos.domain.repository.AiPresetRepository
import com.tenebralis.dreamos.domain.repository.ApiConnectionRepository
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.ConnectionSecretRepository
import com.tenebralis.dreamos.domain.repository.ConversationRepository
import com.tenebralis.dreamos.domain.repository.MessageRepository
import com.tenebralis.dreamos.domain.service.AiChatService
import com.tenebralis.dreamos.domain.usecase.event.GameEventParser
import com.tenebralis.dreamos.domain.usecase.event.ProcessGameEventsUseCase
import com.tenebralis.dreamos.domain.usecase.context.SaveContextLogUseCase
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * 完整消息发送用例：user 落库 → 组装上下文 → AI 调用 → assistant 落库。
 *
 * 设计要点：
 * - user 消息**始终落库**（发送即持久化）
 * - assistant 消息**仅在 AI 成功后才落库**
 * - AI 失败时返回 [SendMessageResult.aiError]，不抛异常
 *
 * M4-P2 新增：[invokeStream] 支持流式 AI 回复，通过 [StreamEvent] 逐步通知 UI。
 */
class SendMessageUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository,
    private val apiConnectionRepository: ApiConnectionRepository,
    private val connectionSecretRepository: ConnectionSecretRepository,
    private val aiPresetRepository: AiPresetRepository,
    private val buildChatContextUseCase: BuildChatContextUseCase,
    private val aiChatService: AiChatService,
    private val updateConversationLastMessageUseCase: UpdateConversationLastMessageUseCase,
    private val gameEventParser: GameEventParser,
    private val processGameEventsUseCase: ProcessGameEventsUseCase,
    private val saveContextLogUseCase: SaveContextLogUseCase
) {

    /**
     * 非流式发送（M4-P1，保留兼容）。
     */
    suspend operator fun invoke(
        conversationId: String,
        content: String
    ): Result<SendMessageResult> = runCatching {
        val normalizedConversationId = conversationId.trim()
        val normalizedContent = content.trim()
        require(normalizedConversationId.isNotEmpty()) { "conversationId 不能为空" }
        require(normalizedContent.isNotEmpty()) { "消息内容不能为空" }

        val userId = authRepository.getCurrentUserId()
            ?: throw IllegalStateException("当前未登录")

        // ── 1. user 消息落库 ──
        val userMessage = sendUserMessage(
            userId = userId,
            conversationId = normalizedConversationId,
            content = normalizedContent
        )

        // ── 2. 获取会话绑定的 API 连接（或全局 active 连接）+ API Key ──
        val connection = loadApiConnection(normalizedConversationId)
            ?: throw NoConnectionException("请先在设置中配置 API 连接")

        val apiKey = connectionSecretRepository.getSecret(connection.id).getOrThrow()
            ?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw NoApiKeyException("请先保存 API Key")

        // ── 3. 组装上下文 ──
        val contextMessages = buildChatContextUseCase(
            conversationId = normalizedConversationId
        ).getOrThrow()

        // ── 3.5 加载会话绑定 Preset 的采样参数 ──
        val samplingParams = loadSamplingParams(normalizedConversationId)

        // ── 4. AI 调用 ──
        val aiResult = aiChatService.chatCompletion(
            connection = connection,
            apiKey = apiKey,
            messages = contextMessages,
            samplingParams = samplingParams
        )

        if (aiResult.isFailure) {
            val errorMessage = aiResult.exceptionOrNull()?.message ?: "AI 调用失败"
            return@runCatching SendMessageResult(
                userMessage = userMessage,
                assistantMessage = null,
                aiError = errorMessage
            )
        }

        val response = aiResult.getOrThrow()
        val rawAssistantContent = response.choices.firstOrNull()?.message?.content?.trim()
        if (rawAssistantContent.isNullOrEmpty()) {
            return@runCatching SendMessageResult(
                userMessage = userMessage,
                assistantMessage = null,
                aiError = "AI 返回了空回复"
            )
        }

        // ── 5. 解析游戏事件并清理内容 ──
        val parseResult = gameEventParser.parse(rawAssistantContent)
        val assistantContent = parseResult.cleanContent.ifEmpty { rawAssistantContent }

        // ── 6. assistant 消息落库（使用清理后的内容） ──
        val assistantMessage = sendAssistantMessage(
            userId = userId,
            conversationId = normalizedConversationId,
            content = assistantContent
        )

        // ── 7. 更新会话 lastMessageAt + summary ──
        updateConversationLastMessageUseCase(
            conversationId = normalizedConversationId,
            lastMessageAt = Instant.now().toString(),
            summary = buildSummary(assistantContent)
        ).getOrThrow()

        // ── 8. 处理游戏事件（最佳努力，不阻断） ──
        if (parseResult.events.isNotEmpty()) {
            runCatching { processGameEventsUseCase(parseResult.events) }
                .onFailure { Log.w("SendMessage", "游戏事件处理失败", it) }
        }

        SendMessageResult(
            userMessage = userMessage,
            assistantMessage = assistantMessage,
            aiError = null
        )
    }

    /**
     * 流式发送（M4-P2）。
     *
     * 返回 [Flow]<[StreamEvent]>，ViewModel 收集该 Flow 即可逐步更新 UI。
     * - 支持 `Job.cancel()` 中断：中断时自动将已接收的部分内容落库为 assistant 消息。
     * - user 消息始终优先落库，AI 失败不影响已有消息。
     */
    fun invokeStream(
        conversationId: String,
        content: String
    ): Flow<StreamEvent> = flow {
        val normalizedConversationId = conversationId.trim()
        val normalizedContent = content.trim()
        require(normalizedConversationId.isNotEmpty()) { "conversationId 不能为空" }
        require(normalizedContent.isNotEmpty()) { "消息内容不能为空" }

        val userId = authRepository.getCurrentUserId()
            ?: throw IllegalStateException("当前未登录")

        // ── 1. user 消息落库 ──
        val userMessage = sendUserMessage(
            userId = userId,
            conversationId = normalizedConversationId,
            content = normalizedContent
        )
        emit(StreamEvent.UserMessageSaved(userMessage))

        // ── 2. 获取会话绑定的 API 连接（或全局 active 连接）+ API Key ──
        val connection = loadApiConnection(normalizedConversationId)
        if (connection == null) {
            emit(StreamEvent.AiError("请先在设置中配置 API 连接"))
            return@flow
        }

        val apiKey = connectionSecretRepository.getSecret(connection.id).getOrThrow()
            ?.trim()?.takeIf { it.isNotEmpty() }
        if (apiKey == null) {
            emit(StreamEvent.AiError("请先保存 API Key"))
            return@flow
        }

        // ── 3. 组装上下文 ──
        val contextMessages = try {
            buildChatContextUseCase(
                conversationId = normalizedConversationId
            ).getOrThrow()
        } catch (e: Throwable) {
            emit(StreamEvent.AiError("上下文组装失败：${e.message ?: "未知错误"}"))
            return@flow
        }

        // ── 3.1 异步保存上下文日志 ──
        runCatching {
            val fullPrompt = contextMessages.joinToString("\n") { "[${it.role}] ${it.content}" }
            saveContextLogUseCase(
                conversationId = normalizedConversationId,
                layers = buildChatContextUseCase.lastLayers,
                fullPromptText = fullPrompt
            )
        }

        // ── 3.5 加载会话绑定 Preset 的采样参数 ──
        val samplingParams = loadSamplingParams(normalizedConversationId)

        // ── 4. 根据 Preset 的 stream_openai 选择流式/非流式 ──
        val streamEnabled = samplingParams["stream_openai"]
            ?.jsonPrimitive?.booleanOrNull ?: true

        if (!streamEnabled) {
            // 非流式路径：一次性获取完整回复，仍用 StreamEvent 通知 UI
            val aiResult = aiChatService.chatCompletion(
                connection = connection,
                apiKey = apiKey,
                messages = contextMessages,
                samplingParams = samplingParams
            )
            if (aiResult.isFailure) {
                val errorMessage = aiResult.exceptionOrNull()?.message ?: "AI 调用失败"
                emit(StreamEvent.AiError(errorMessage))
                return@flow
            }
            val response = aiResult.getOrThrow()
            val rawContent = response.choices.firstOrNull()?.message?.content?.trim()
            if (rawContent.isNullOrEmpty()) {
                emit(StreamEvent.AiError("AI 返回了空回复"))
                return@flow
            }
            val parseResult = gameEventParser.parse(rawContent)
            val assistantContent = parseResult.cleanContent.ifEmpty { rawContent }
            val assistantMessage = sendAssistantMessage(
                userId = userId,
                conversationId = normalizedConversationId,
                content = assistantContent
            )
            updateConversationLastMessageUseCase(
                conversationId = normalizedConversationId,
                lastMessageAt = Instant.now().toString(),
                summary = buildSummary(assistantContent)
            ).getOrThrow()
            if (parseResult.events.isNotEmpty()) {
                runCatching { processGameEventsUseCase(parseResult.events) }
                    .onFailure { Log.w("SendMessage", "游戏事件处理失败", it) }
            }
            emit(StreamEvent.AiCompleted(assistantMessage))
            return@flow
        }

        // 流式路径
        val accumulated = StringBuilder()
        var hasError = false

        try {
            aiChatService.chatCompletionStream(
                connection = connection,
                apiKey = apiKey,
                messages = contextMessages,
                samplingParams = samplingParams
            ).collect { result ->
                // 在每个 chunk 之间检查协程是否被取消
                currentCoroutineContext().ensureActive()

                result.fold(
                    onSuccess = { chunk ->
                        accumulated.append(chunk)
                        emit(StreamEvent.AiChunk(accumulated.toString()))
                    },
                    onFailure = { error ->
                        hasError = true
                        emit(StreamEvent.AiError(error.message ?: "AI 调用失败"))
                    }
                )

                // 如果已发生错误，停止收集
                if (hasError) return@collect
            }
        } catch (e: CancellationException) {
            // 用户主动取消 → 保存已接收的部分内容
            savePartialAssistant(
                userId = userId,
                conversationId = normalizedConversationId,
                accumulated = accumulated
            )
            throw e // 重新抛出以正确传播取消
        } catch (e: Throwable) {
            emit(StreamEvent.AiError("AI 流式调用失败：${e.message ?: "未知错误"}"))
            hasError = true
        }

        if (hasError) {
            // AI 失败但已有部分内容 → 也保存部分内容
            savePartialAssistant(
                userId = userId,
                conversationId = normalizedConversationId,
                accumulated = accumulated
            )
            return@flow
        }

        // ── 5. 流正常结束：解析事件 + assistant 消息落库 ──
        val rawFinalContent = accumulated.toString().trim()
        if (rawFinalContent.isEmpty()) {
            emit(StreamEvent.AiError("AI 返回了空回复"))
            return@flow
        }

        val parseResult = gameEventParser.parse(rawFinalContent)
        val finalContent = parseResult.cleanContent.ifEmpty { rawFinalContent }

        val assistantMessage = sendAssistantMessage(
            userId = userId,
            conversationId = normalizedConversationId,
            content = finalContent
        )

        // ── 6. 更新会话 lastMessageAt + summary ──
        updateConversationLastMessageUseCase(
            conversationId = normalizedConversationId,
            lastMessageAt = Instant.now().toString(),
            summary = buildSummary(finalContent)
        ).getOrThrow()

        // ── 7. 处理游戏事件 ──
        if (parseResult.events.isNotEmpty()) {
            runCatching { processGameEventsUseCase(parseResult.events) }
                .onFailure { Log.w("SendMessage", "游戏事件处理失败", it) }
        }

        emit(StreamEvent.AiCompleted(assistantMessage))
    }

    // ── 内部辅助方法 ──

    /**
     * 从会话绑定的 Preset 中加载采样参数。
     * 优先使用会话的 presetId；若无则回退到用户首个 Preset；
     * 均无时返回空 JsonObject（使用 AI Service 默认值）。
     */
    private suspend fun loadSamplingParams(conversationId: String): JsonObject {
        return runCatching {
            // 优先：从会话绑定的 preset 加载
            val conversation = conversationRepository.getById(conversationId).getOrThrow()
            val presetId = conversation.presetId
            if (presetId != null) {
                val preset = aiPresetRepository.getById(presetId).getOrNull()
                if (preset != null) return@runCatching preset.presetJson
            }
            // 回退：用户首个 preset
            val presets = aiPresetRepository.getByUser().first().getOrThrow()
            presets.firstOrNull()?.presetJson ?: JsonObject(emptyMap())
        }.getOrDefault(JsonObject(emptyMap()))
    }

    /**
     * 加载会话绑定的 API 连接。
     * 优先使用会话的 apiConnectionId；若无则回退到全局 active 连接。
     */
    private suspend fun loadApiConnection(conversationId: String): com.tenebralis.dreamos.domain.model.ApiConnection? {
        return runCatching {
            val conversation = conversationRepository.getById(conversationId).getOrThrow()
            val connId = conversation.apiConnectionId
            if (connId != null) {
                // 尝试加载指定连接
                val allConnections = apiConnectionRepository.getAll()
                    .first().getOrThrow()
                val specificConnection = allConnections.firstOrNull { it.id == connId }
                if (specificConnection != null) return@runCatching specificConnection
            }
            // 回退：全局 active
            apiConnectionRepository.getActive().getOrThrow()
        }.getOrNull()
    }


    /**
     * 保存流式生成中的部分 assistant 内容（中断或失败时使用）。
     */
    private suspend fun savePartialAssistant(
        userId: String,
        conversationId: String,
        accumulated: StringBuilder
    ) {
        val partial = accumulated.toString().trim()
        if (partial.isEmpty()) return

        runCatching {
            sendAssistantMessage(userId, conversationId, partial)
            updateConversationLastMessageUseCase(
                conversationId = conversationId,
                lastMessageAt = Instant.now().toString(),
                summary = buildSummary(partial)
            ).getOrThrow()
        }
        // 保存部分消息失败不阻断主流程
    }

    private suspend fun sendUserMessage(
        userId: String,
        conversationId: String,
        content: String
    ): ConversationMessage {
        var retryCount = 0
        while (true) {
            val nextSeq = messageRepository.getNextSeq(conversationId).getOrThrow()
            val draft = ConversationMessage(
                id = UUID.randomUUID().toString(),
                userId = userId,
                conversationId = conversationId,
                seq = nextSeq,
                role = MessageRole.USER,
                content = content,
                metadataJson = JsonObject(emptyMap()),
                createdAt = null
            )

            val sentResult = messageRepository.send(draft)
            if (sentResult.isSuccess) {
                // 更新会话时间戳
                updateConversationLastMessageUseCase(
                    conversationId = conversationId,
                    lastMessageAt = Instant.now().toString(),
                    summary = buildSummary(content)
                ).getOrThrow()
                return sentResult.getOrThrow()
            }

            val error = sentResult.exceptionOrNull() ?: IllegalStateException("消息发送失败")
            if (isSeqConflict(error) && retryCount < MAX_SEQ_RETRY_COUNT) {
                retryCount += 1
                continue
            }
            throw error
        }
    }

    private suspend fun sendAssistantMessage(
        userId: String,
        conversationId: String,
        content: String
    ): ConversationMessage {
        var retryCount = 0
        while (true) {
            val nextSeq = messageRepository.getNextSeq(conversationId).getOrThrow()
            val draft = ConversationMessage(
                id = UUID.randomUUID().toString(),
                userId = userId,
                conversationId = conversationId,
                seq = nextSeq,
                role = MessageRole.ASSISTANT,
                content = content,
                metadataJson = JsonObject(emptyMap()),
                createdAt = null
            )

            val sentResult = messageRepository.send(draft)
            if (sentResult.isSuccess) {
                return sentResult.getOrThrow()
            }

            val error = sentResult.exceptionOrNull() ?: IllegalStateException("消息发送失败")
            if (isSeqConflict(error) && retryCount < MAX_SEQ_RETRY_COUNT) {
                retryCount += 1
                continue
            }
            throw error
        }
    }

    private fun buildSummary(content: String): String {
        val condensed = content.replace(Regex("\\s+"), " ").trim()
        if (condensed.length <= SUMMARY_MAX_LENGTH) return condensed
        return condensed.take(SUMMARY_MAX_LENGTH)
    }

    private fun isSeqConflict(error: Throwable): Boolean {
        val lowered = error.message.orEmpty().lowercase()
        return "messages_conversation_seq_unique" in lowered || "duplicate key" in lowered
    }

    private companion object {
        const val SUMMARY_MAX_LENGTH = 80
        const val MAX_SEQ_RETRY_COUNT = 3
    }
}

/**
 * 无可用 API 连接时抛出。
 */
class NoConnectionException(message: String) : Exception(message)

/**
 * 无可用 API Key 时抛出。
 */
class NoApiKeyException(message: String) : Exception(message)


