package com.tenebralis.dreamos.domain.usecase.connection

import com.tenebralis.dreamos.domain.repository.ApiConnectionRepository
import javax.inject.Inject

class SetActiveConnectionUseCase @Inject constructor(
    private val repository: ApiConnectionRepository
) {
    suspend operator fun invoke(connectionId: String): Result<Unit> = repository.setActive(connectionId)
}
