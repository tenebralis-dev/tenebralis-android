package com.tenebralis.dreamos.domain.usecase.chat

import com.tenebralis.dreamos.domain.model.Conversation
import com.tenebralis.dreamos.domain.repository.ConversationRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class GetConversationsBySaveUseCase @Inject constructor(
    private val repository: ConversationRepository
) {

    suspend operator fun invoke(saveId: String): Result<List<Conversation>> {
        require(saveId.trim().isNotEmpty()) { "saveId 不能为空" }
        return repository.getBySave(saveId).first()
    }
}
