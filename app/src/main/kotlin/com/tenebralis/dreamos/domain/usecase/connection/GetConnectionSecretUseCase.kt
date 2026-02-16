package com.tenebralis.dreamos.domain.usecase.connection

import com.tenebralis.dreamos.domain.repository.ConnectionSecretRepository
import javax.inject.Inject

class GetConnectionSecretUseCase @Inject constructor(
    private val repository: ConnectionSecretRepository
) {
    suspend operator fun invoke(connectionId: String): Result<String?> = repository.getSecret(connectionId)
}
