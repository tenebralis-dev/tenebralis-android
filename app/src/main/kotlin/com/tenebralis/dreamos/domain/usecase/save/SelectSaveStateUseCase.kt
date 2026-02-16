package com.tenebralis.dreamos.domain.usecase.save

import com.tenebralis.dreamos.domain.model.WorldSaveState
import com.tenebralis.dreamos.domain.repository.SaveStateRepository
import java.time.Instant
import javax.inject.Inject

class SelectSaveStateUseCase @Inject constructor(
    private val repository: SaveStateRepository
) {

    suspend operator fun invoke(saveState: WorldSaveState): Result<WorldSaveState> = runCatching {
        require(saveState.id.isNotBlank()) { "saveState.id 不能为空" }
        require(saveState.identityId.isNotBlank()) { "saveState.identityId 不能为空" }

        repository.update(
            saveState.copy(lastPlayedAt = Instant.now().toString())
        ).getOrThrow()
    }
}
