package com.tenebralis.dreamos.domain.usecase.connection

import com.tenebralis.dreamos.domain.repository.ConnectionSecretRepository
import javax.inject.Inject

class SaveConnectionSecretUseCase @Inject constructor(
    private val repository: ConnectionSecretRepository
) {

    suspend operator fun invoke(connectionId: String, apiKey: String): Result<Unit> {
        val normalized = apiKey.trim()
        return if (normalized.isEmpty()) {
            repository.clearSecret(connectionId)
        } else {
            repository.saveSecret(connectionId, normalized)
        }
    }
}
