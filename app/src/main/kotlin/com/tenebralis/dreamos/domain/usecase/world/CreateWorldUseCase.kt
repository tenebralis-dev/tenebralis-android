package com.tenebralis.dreamos.domain.usecase.world

import com.tenebralis.dreamos.domain.model.World
import com.tenebralis.dreamos.domain.model.enums.WorldStatus
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.WorldRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.serialization.json.JsonObject

class CreateWorldUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val repository: WorldRepository
) {

    suspend operator fun invoke(
        name: String,
        description: String?
    ): Result<World> = runCatching {
        val normalizedName = name.trim()
        require(normalizedName.isNotEmpty()) { "世界名称不能为空" }

        val userId = authRepository.getCurrentUserId()
            ?: throw IllegalStateException("当前未登录")

        val world = World(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = normalizedName,
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            status = WorldStatus.ACTIVE,
            promptLoreText = null,
            loreJson = JsonObject(emptyMap()),
            rulesJson = JsonObject(emptyMap()),
            aiContextJson = JsonObject(emptyMap()),
            createdAt = null,
            updatedAt = null
        )

        repository.create(world).getOrThrow()
    }
}
