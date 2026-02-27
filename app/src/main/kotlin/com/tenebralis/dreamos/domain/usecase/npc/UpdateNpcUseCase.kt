package com.tenebralis.dreamos.domain.usecase.npc

import com.tenebralis.dreamos.domain.model.Npc
import com.tenebralis.dreamos.domain.repository.NpcRepository
import javax.inject.Inject

/**
 * 更新 NPC
 */
class UpdateNpcUseCase @Inject constructor(
    private val npcRepository: NpcRepository
) {

    suspend operator fun invoke(npc: Npc): Result<Npc> = runCatching {
        require(npc.id.isNotBlank()) { "npc.id 不能为空" }
        require(npc.name.trim().isNotEmpty()) { "NPC 名称不能为空" }
        npcRepository.update(npc).getOrThrow()
    }
}
