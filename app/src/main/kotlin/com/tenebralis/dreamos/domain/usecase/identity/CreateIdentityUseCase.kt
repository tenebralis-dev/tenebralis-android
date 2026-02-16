package com.tenebralis.dreamos.domain.usecase.identity

import com.tenebralis.dreamos.domain.model.UserWorldIdentity
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.IdentityRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.serialization.json.JsonObject

class CreateIdentityUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val repository: IdentityRepository
) {

    suspend operator fun invoke(
        worldId: String,
        identityName: String,
        setActive: Boolean
    ): Result<UserWorldIdentity> = runCatching {
        val normalizedWorldId = worldId.trim()
        val normalizedName = identityName.trim()

        require(normalizedWorldId.isNotEmpty()) { "worldId 不能为空" }
        require(normalizedName.isNotEmpty()) { "身份名称不能为空" }

        val userId = authRepository.getCurrentUserId()
            ?: throw IllegalStateException("当前未登录")

        val identity = UserWorldIdentity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            worldId = normalizedWorldId,
            identityName = normalizedName,
            isActive = setActive,
            promptIdentityText = null,
            roleDataJson = JsonObject(emptyMap()),
            personaJson = JsonObject(emptyMap()),
            createdAt = null,
            updatedAt = null
        )

        repository.create(identity).getOrThrow()
    }
}
