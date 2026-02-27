package com.tenebralis.dreamos.domain.usecase.npc

import com.tenebralis.dreamos.domain.model.Npc
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.NpcRepository
import kotlinx.serialization.json.JsonObject
import java.util.UUID
import javax.inject.Inject

/**
 * 手动创建 NPC
 */
class CreateNpcUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val npcRepository: NpcRepository
) {

    suspend operator fun invoke(
        name: String,
        description: String?
    ): Result<Npc> = runCatching {
        val trimmedName = name.trim()
        require(trimmedName.isNotEmpty()) { "NPC 名称不能为空" }

        val userId = authRepository.getCurrentUserId()
            ?: throw IllegalStateException("当前未登录")

        val npc = Npc(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = trimmedName,
            description = description?.trim(),
            promptNpcText = null,
            personaJson = JsonObject(emptyMap()),
            createdAt = null,
            updatedAt = null
        )

        npcRepository.create(npc).getOrThrow()
    }
}
