package com.tenebralis.dreamos.domain.usecase.connection

import com.tenebralis.dreamos.domain.model.ApiConnection
import com.tenebralis.dreamos.domain.repository.ApiConnectionRepository
import com.tenebralis.dreamos.domain.repository.AuthRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.serialization.json.JsonObject

class CreateConnectionUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val repository: ApiConnectionRepository,
    private val validator: ConnectionValidator
) {

    suspend operator fun invoke(
        draft: ConnectionDraft,
        isActive: Boolean
    ): Result<ApiConnection> = runCatching {
        val userId = authRepository.getCurrentUserId()
            ?: throw IllegalStateException("当前未登录")

        val connection = ApiConnection(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = draft.name.trim(),
            serviceType = draft.serviceType.trim(),
            baseUrl = normalizeBaseUrl(draft.baseUrl),
            isSynced = true,
            isActive = isActive,
            defaultModel = draft.defaultModel?.trim()?.takeIf { it.isNotEmpty() },
            systemPrompt = null,
            paramsJson = JsonObject(emptyMap()),
            headersTemplateJson = draft.headersTemplateJson,
            configJson = JsonObject(emptyMap()),
            createdAt = null,
            updatedAt = null
        )

        validator.validate(connection).getOrThrow()
        repository.create(connection).getOrThrow()
    }

    private fun normalizeBaseUrl(baseUrl: String): String {
        return baseUrl.trim().trimEnd('/')
    }
}
