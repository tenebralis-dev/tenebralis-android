package com.tenebralis.dreamos.domain.usecase.identity

import com.tenebralis.dreamos.domain.model.UserWorldIdentity
import com.tenebralis.dreamos.domain.repository.IdentityRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class GetIdentitiesUseCase @Inject constructor(
    private val repository: IdentityRepository
) {
    suspend operator fun invoke(worldId: String): Result<List<UserWorldIdentity>> {
        require(worldId.isNotBlank()) { "worldId 不能为空" }
        return repository.getByWorld(worldId).first()
    }
}
