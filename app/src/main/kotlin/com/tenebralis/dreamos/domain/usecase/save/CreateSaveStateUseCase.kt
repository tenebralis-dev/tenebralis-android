package com.tenebralis.dreamos.domain.usecase.save

import com.tenebralis.dreamos.domain.model.WorldSaveState
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.SaveStateRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.serialization.json.JsonObject

class CreateSaveStateUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val repository: SaveStateRepository
) {

    suspend operator fun invoke(
        worldId: String,
        identityId: String,
        slot: Int,
        title: String?
    ): Result<WorldSaveState> = runCatching {
        val normalizedWorldId = worldId.trim()
        val normalizedIdentityId = identityId.trim()

        require(normalizedWorldId.isNotEmpty()) { "worldId 不能为空" }
        require(normalizedIdentityId.isNotEmpty()) { "identityId 不能为空" }
        require(slot > 0) { "slot 必须大于 0" }

        val userId = authRepository.getCurrentUserId()
            ?: throw IllegalStateException("当前未登录")

        val saveState = WorldSaveState(
            id = UUID.randomUUID().toString(),
            userId = userId,
            worldId = normalizedWorldId,
            identityId = normalizedIdentityId,
            slot = slot,
            title = title?.trim()?.takeIf { it.isNotEmpty() },
            summary = null,
            chapter = null,
            stage = null,
            promptProgressText = null,
            stateJson = JsonObject(emptyMap()),
            lastPlayedAt = null,
            createdAt = null,
            updatedAt = null
        )

        repository.create(saveState).getOrThrow()
    }
}
