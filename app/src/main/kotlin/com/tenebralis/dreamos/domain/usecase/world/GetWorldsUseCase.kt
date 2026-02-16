package com.tenebralis.dreamos.domain.usecase.world

import com.tenebralis.dreamos.domain.model.World
import com.tenebralis.dreamos.domain.repository.WorldRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class GetWorldsUseCase @Inject constructor(
    private val repository: WorldRepository
) {
    suspend operator fun invoke(): Result<List<World>> = repository.getWorlds().first()
}
