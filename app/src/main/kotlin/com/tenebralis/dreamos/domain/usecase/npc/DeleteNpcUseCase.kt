package com.tenebralis.dreamos.domain.usecase.npc

import com.tenebralis.dreamos.domain.repository.NpcRepository
import javax.inject.Inject

/**
 * 删除 NPC
 */
class DeleteNpcUseCase @Inject constructor(
    private val npcRepository: NpcRepository
) {

    suspend operator fun invoke(npcId: String): Result<Unit> = runCatching {
        require(npcId.isNotBlank()) { "npcId 不能为空" }
        npcRepository.delete(npcId).getOrThrow()
    }
}
