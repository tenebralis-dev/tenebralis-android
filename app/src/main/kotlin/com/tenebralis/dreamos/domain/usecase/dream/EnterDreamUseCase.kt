package com.tenebralis.dreamos.domain.usecase.dream

import com.tenebralis.dreamos.domain.model.DreamSession
import com.tenebralis.dreamos.domain.repository.IdentityRepository
import com.tenebralis.dreamos.domain.repository.SaveStateRepository
import com.tenebralis.dreamos.domain.repository.WorldRepository
import com.tenebralis.dreamos.domain.usecase.chat.GetOrCreateConversationUseCase
import javax.inject.Inject

/**
 * 进入梦境用例。
 *
 * 根据 saveId 加载完整上下文（世界、身份、存档），
 * 获取或创建叙事者 NPC 和叙事线程，返回 [DreamSession]。
 */
class EnterDreamUseCase @Inject constructor(
    private val saveStateRepository: SaveStateRepository,
    private val identityRepository: IdentityRepository,
    private val worldRepository: WorldRepository,
    private val getOrCreateNarratorNpcUseCase: GetOrCreateNarratorNpcUseCase,
    private val getOrCreateConversationUseCase: GetOrCreateConversationUseCase
) {

    suspend operator fun invoke(saveId: String): Result<DreamSession> = runCatching {
        val normalizedSaveId = saveId.trim()
        require(normalizedSaveId.isNotEmpty()) { "saveId 不能为空" }

        // 1. 加载存档
        val save = saveStateRepository.getById(normalizedSaveId).getOrThrow()

        // 2. 回溯身份和世界
        val identity = identityRepository.getById(save.identityId).getOrThrow()
        val world = worldRepository.getById(save.worldId).getOrThrow()

        // 3. 获取或创建叙事者 NPC
        val narratorNpc = getOrCreateNarratorNpcUseCase().getOrThrow()

        // 4. 获取或创建叙事线程
        val conversation = getOrCreateConversationUseCase(
            saveId = normalizedSaveId,
            npcId = narratorNpc.id,
            threadKey = GetOrCreateNarratorNpcUseCase.DREAM_THREAD_KEY
        ).getOrThrow()

        DreamSession(
            world = world,
            identity = identity,
            save = save,
            conversation = conversation,
            narratorNpc = narratorNpc
        )
    }
}
