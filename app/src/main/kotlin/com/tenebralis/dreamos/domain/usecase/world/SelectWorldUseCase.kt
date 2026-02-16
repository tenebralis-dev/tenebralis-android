package com.tenebralis.dreamos.domain.usecase.world

import com.tenebralis.dreamos.domain.model.World
import com.tenebralis.dreamos.domain.repository.WorldRepository
import javax.inject.Inject

class SelectWorldUseCase @Inject constructor(
    private val repository: WorldRepository
) {

    suspend operator fun invoke(worldId: String): Result<World> = runCatching {
        require(worldId.isNotBlank()) { "worldId 不能为空" }

        // 用一次 update 触发 updated_at，作为最近选择世界的恢复依据。
        val world = repository.getById(worldId).getOrThrow()
        repository.update(world).getOrThrow()
    }
}
