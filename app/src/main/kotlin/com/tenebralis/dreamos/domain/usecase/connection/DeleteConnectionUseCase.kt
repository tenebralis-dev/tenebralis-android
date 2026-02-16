package com.tenebralis.dreamos.domain.usecase.connection

import com.tenebralis.dreamos.domain.repository.ApiConnectionRepository
import com.tenebralis.dreamos.domain.repository.ConnectionSecretRepository
import javax.inject.Inject

class DeleteConnectionUseCase @Inject constructor(
    private val apiConnectionRepository: ApiConnectionRepository,
    private val connectionSecretRepository: ConnectionSecretRepository
) {

    suspend operator fun invoke(connectionId: String): Result<Unit> = runCatching {
        apiConnectionRepository.delete(connectionId).getOrThrow()
        connectionSecretRepository.clearSecret(connectionId).getOrThrow()
    }
}
