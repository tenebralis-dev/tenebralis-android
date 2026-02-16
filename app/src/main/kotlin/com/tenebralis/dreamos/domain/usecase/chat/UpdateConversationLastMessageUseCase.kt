package com.tenebralis.dreamos.domain.usecase.chat

import com.tenebralis.dreamos.domain.repository.ConversationRepository
import javax.inject.Inject

class UpdateConversationLastMessageUseCase @Inject constructor(
    private val repository: ConversationRepository
) {

    suspend operator fun invoke(
        conversationId: String,
        lastMessageAt: String,
        summary: String?
    ): Result<Unit> = runCatching {
        require(conversationId.trim().isNotEmpty()) { "conversationId 不能为空" }
        require(lastMessageAt.trim().isNotEmpty()) { "lastMessageAt 不能为空" }

        repository.updateLastMessage(
            conversationId = conversationId,
            lastMessageAt = lastMessageAt,
            summary = summary
        ).getOrThrow()
    }
}
