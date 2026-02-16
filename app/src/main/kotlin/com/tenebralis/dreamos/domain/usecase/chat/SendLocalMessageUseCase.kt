package com.tenebralis.dreamos.domain.usecase.chat

import com.tenebralis.dreamos.domain.model.ConversationMessage
import com.tenebralis.dreamos.domain.model.enums.MessageRole
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.MessageRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.serialization.json.JsonObject

class SendLocalMessageUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val messageRepository: MessageRepository,
    private val updateConversationLastMessageUseCase: UpdateConversationLastMessageUseCase
) {

    suspend operator fun invoke(
        conversationId: String,
        content: String
    ): Result<ConversationMessage> = runCatching {
        val normalizedConversationId = conversationId.trim()
        val normalizedContent = content.trim()
        require(normalizedConversationId.isNotEmpty()) { "conversationId 不能为空" }
        require(normalizedContent.isNotEmpty()) { "消息内容不能为空" }

        val userId = authRepository.getCurrentUserId()
            ?: throw IllegalStateException("当前未登录")

        var retryCount = 0
        while (true) {
            val nextSeq = messageRepository.getNextSeq(normalizedConversationId).getOrThrow()
            val draft = ConversationMessage(
                id = UUID.randomUUID().toString(),
                userId = userId,
                conversationId = normalizedConversationId,
                seq = nextSeq,
                role = MessageRole.USER,
                content = normalizedContent,
                metadataJson = JsonObject(emptyMap()),
                createdAt = null
            )

            val sentResult = messageRepository.send(draft)
            if (sentResult.isSuccess) {
                val sent = sentResult.getOrThrow()
                updateConversationLastMessageUseCase(
                    conversationId = normalizedConversationId,
                    lastMessageAt = Instant.now().toString(),
                    summary = buildSummary(normalizedContent)
                ).getOrThrow()
                return@runCatching sent
            }

            val error = sentResult.exceptionOrNull() ?: IllegalStateException("消息发送失败")
            if (isSeqConflict(error) && retryCount < MAX_SEQ_RETRY_COUNT) {
                retryCount += 1
                continue
            }
            throw error
        }

        throw IllegalStateException("Unreachable retry loop state")
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
