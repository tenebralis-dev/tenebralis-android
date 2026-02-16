package com.tenebralis.dreamos.domain.usecase.save

import com.tenebralis.dreamos.domain.model.WorldSaveState
import com.tenebralis.dreamos.domain.repository.SaveStateRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class GetSaveStatesUseCase @Inject constructor(
    private val repository: SaveStateRepository
) {
    suspend operator fun invoke(identityId: String): Result<List<WorldSaveState>> {
        require(identityId.isNotBlank()) { "identityId 不能为空" }
        return repository.getByIdentity(identityId).first()
    }
}
