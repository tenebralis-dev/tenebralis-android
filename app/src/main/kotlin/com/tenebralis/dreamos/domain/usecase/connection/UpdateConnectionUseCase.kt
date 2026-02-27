package com.tenebralis.dreamos.domain.usecase.connection

import com.tenebralis.dreamos.domain.model.ApiConnection
import com.tenebralis.dreamos.domain.repository.ApiConnectionRepository
import javax.inject.Inject

class UpdateConnectionUseCase @Inject constructor(
    private val repository: ApiConnectionRepository,
    private val validator: ConnectionValidator
) {

    suspend operator fun invoke(
        origin: ApiConnection,
        draft: ConnectionDraft
    ): Result<ApiConnection> = runCatching {
        val updated = origin.copy(
            name = draft.name.trim(),
            serviceType = draft.serviceType.trim(),
            baseUrl = normalizeBaseUrl(draft.baseUrl),
            defaultModel = draft.defaultModel?.trim()?.takeIf { it.isNotEmpty() },
            headersTemplateJson = draft.headersTemplateJson
        )

        validator.validate(updated).getOrThrow()
        repository.update(updated).getOrThrow()
    }

    private fun normalizeBaseUrl(baseUrl: String): String {
        return baseUrl.trim().trimEnd('/')
    }
}
