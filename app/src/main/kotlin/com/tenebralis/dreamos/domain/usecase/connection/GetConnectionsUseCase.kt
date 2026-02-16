package com.tenebralis.dreamos.domain.usecase.connection

import com.tenebralis.dreamos.domain.model.ApiConnection
import com.tenebralis.dreamos.domain.repository.ApiConnectionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class GetConnectionsUseCase @Inject constructor(
    private val repository: ApiConnectionRepository
) {
    suspend operator fun invoke(): Result<List<ApiConnection>> = repository.getAll().first()
}
