package com.tenebralis.dreamos.domain.usecase.chat

import com.tenebralis.dreamos.domain.model.Conversation
import com.tenebralis.dreamos.domain.repository.ConversationRepository
import javax.inject.Inject

class GetOrCreateConversationUseCase @Inject constructor(
    private val repository: ConversationRepository
) {

    suspend operator fun invoke(
        saveId: String,
        npcId: String,
        threadKey: String = DEFAULT_THREAD_KEY
    ): Result<Conversation> = runCatching {
        val normalizedSaveId = saveId.trim()
        val normalizedNpcId = npcId.trim()
        val normalizedThreadKey = threadKey.trim()

        require(normalizedSaveId.isNotEmpty()) { "saveId 不能为空" }
        require(normalizedNpcId.isNotEmpty()) { "npcId 不能为空" }
        require(normalizedThreadKey.isNotEmpty()) { "threadKey 不能为空" }

        repository.getOrCreate(
            saveId = normalizedSaveId,
            npcId = normalizedNpcId,
            threadKey = normalizedThreadKey
        ).getOrThrow()
    }

    companion object {
        const val DEFAULT_THREAD_KEY = "main"
    }
}
