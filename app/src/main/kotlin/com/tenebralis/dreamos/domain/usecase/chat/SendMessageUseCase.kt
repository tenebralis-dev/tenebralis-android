package com.tenebralis.dreamos.domain.usecase.chat

import com.tenebralis.dreamos.domain.model.ConversationMessage
import com.tenebralis.dreamos.domain.model.enums.MessageRole
import com.tenebralis.dreamos.domain.repository.ApiConnectionRepository
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.ConnectionSecretRepository
import com.tenebralis.dreamos.domain.repository.MessageRepository
import com.tenebralis.dreamos.domain.service.AiChatService
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.serialization.json.JsonObject

/**
 * 完整消息发送用例：user 落库 → 组装上下文 → AI 调用 → assistant 落库。
 *
 * 设计要点：
 * - user 消息**始终落库**（发送即持久化）
 * - assistant 消息**仅在 AI 成功后才落库**
 * - AI 失败时返回 [SendMessageResult.aiError]，不抛异常
 */
class SendMessageUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val messageRepository: MessageRepository,
    private val apiConnectionRepository: ApiConnectionRepository,
    private val connectionSecretRepository: ConnectionSecretRepository,
    private val buildChatContextUseCase: BuildChatContextUseCase,
    private val aiChatService: AiChatService,
    private val updateConversationLastMessageUseCase: UpdateConversationLastMessageUseCase
) {

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

        // ── 2. 获取 active connection + API Key ──
        val connection = apiConnectionRepository.getActive().getOrThrow()
            ?: throw NoConnectionException("请先在设置中配置 API 连接")

        val apiKey = connectionSecretRepository.getSecret(connection.id).getOrThrow()
            ?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw NoApiKeyException("请先保存 API Key")

        // ── 3. 组装上下文 ──
        val contextMessages = buildChatContextUseCase(
            conversationId = normalizedConversationId,
            systemPrompt = connection.systemPrompt
        ).getOrThrow()

        // ── 4. AI 调用 ──
        val aiResult = aiChatService.chatCompletion(
            connection = connection,
            apiKey = apiKey,
            messages = contextMessages
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
        val assistantContent = response.choices.firstOrNull()?.message?.content?.trim()
        if (assistantContent.isNullOrEmpty()) {
            return@runCatching SendMessageResult(
                userMessage = userMessage,
                assistantMessage = null,
                aiError = "AI 返回了空回复"
            )
        }

        // ── 5. assistant 消息落库 ──
        val assistantMessage = sendAssistantMessage(
            userId = userId,
            conversationId = normalizedConversationId,
            content = assistantContent
        )

        // ── 6. 更新会话 lastMessageAt + summary ──
        updateConversationLastMessageUseCase(
            conversationId = normalizedConversationId,
            lastMessageAt = Instant.now().toString(),
            summary = buildSummary(assistantContent)
        ).getOrThrow()

        SendMessageResult(
            userMessage = userMessage,
            assistantMessage = assistantMessage,
            aiError = null
        )
    }

    // ── 内部辅助方法 ──

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
