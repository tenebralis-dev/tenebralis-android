package com.tenebralis.dreamos.domain.usecase.chat

import com.tenebralis.dreamos.domain.model.ConversationMessage
import com.tenebralis.dreamos.domain.repository.MessageRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class GetMessagesUseCase @Inject constructor(
    private val repository: MessageRepository
) {

    suspend operator fun invoke(conversationId: String): Result<List<ConversationMessage>> {
        require(conversationId.trim().isNotEmpty()) { "conversationId 不能为空" }
        return repository.getByConversation(conversationId).first()
    }
}
