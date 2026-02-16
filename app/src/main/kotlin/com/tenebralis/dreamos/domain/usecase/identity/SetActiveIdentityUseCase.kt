package com.tenebralis.dreamos.domain.usecase.identity

import com.tenebralis.dreamos.domain.repository.IdentityRepository
import javax.inject.Inject

class SetActiveIdentityUseCase @Inject constructor(
    private val repository: IdentityRepository
) {

    suspend operator fun invoke(worldId: String, identityId: String): Result<Unit> = runCatching {
        require(worldId.isNotBlank()) { "worldId 不能为空" }
        require(identityId.isNotBlank()) { "identityId 不能为空" }
        repository.setActive(worldId = worldId, identityId = identityId).getOrThrow()
    }
}
