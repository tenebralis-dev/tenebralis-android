package com.tenebralis.dreamos.domain.usecase.dream

import com.tenebralis.dreamos.domain.usecase.identity.GetIdentitiesUseCase
import com.tenebralis.dreamos.domain.usecase.save.GetSaveStatesUseCase
import com.tenebralis.dreamos.domain.usecase.world.GetWorldsUseCase
import javax.inject.Inject

class ResolveDreamEntryUseCase @Inject constructor(
    private val getWorldsUseCase: GetWorldsUseCase,
    private val getIdentitiesUseCase: GetIdentitiesUseCase,
    private val getSaveStatesUseCase: GetSaveStatesUseCase
) {

    suspend operator fun invoke(): DreamEntryDestination {
        val worlds = getWorldsUseCase().getOrElse { error ->
            return DreamEntryDestination.Error(error.toUserMessage())
        }
        if (worlds.isEmpty()) {
            return DreamEntryDestination.WorldSelection
        }

        val world = worlds.first()
        val worldId = world.id.trim()
        if (worldId.isEmpty()) {
            return DreamEntryDestination.Error("worldId 不能为空")
        }

        val identities = getIdentitiesUseCase(worldId).getOrElse { error ->
            return DreamEntryDestination.Error(error.toUserMessage())
        }
        if (identities.isEmpty()) {
            return DreamEntryDestination.IdentitySelection(worldId = worldId)
        }

        val identity = identities.firstOrNull { it.isActive } ?: identities.first()
        val identityId = identity.id.trim()
        if (identityId.isEmpty()) {
            return DreamEntryDestination.Error("identityId 不能为空")
        }

        val saveStates = getSaveStatesUseCase(identityId).getOrElse { error ->
            return DreamEntryDestination.Error(error.toUserMessage())
        }
        if (saveStates.isEmpty()) {
            return DreamEntryDestination.SaveSelection(
                worldId = worldId,
                identityId = identityId
            )
        }

        val save = saveStates.first()
        val saveId = save.id.trim()
        if (saveId.isEmpty()) {
            return DreamEntryDestination.Error("saveId 不能为空")
        }

        return DreamEntryDestination.ConversationSelection(saveId = saveId)
    }

    private fun Throwable.toUserMessage(): String {
        return message
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: "解析梦境入口失败，请重试"
    }
}
