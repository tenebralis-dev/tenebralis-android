package com.tenebralis.dreamos.domain.usecase.preset

import com.tenebralis.dreamos.domain.repository.AiPresetRepository
import javax.inject.Inject

/**
 * 删除 AI Preset
 */
class DeletePresetUseCase @Inject constructor(
    private val repository: AiPresetRepository
) {
    suspend operator fun invoke(presetId: String): Result<Unit> = runCatching {
        require(presetId.isNotBlank()) { "presetId 不能为空" }
        repository.delete(presetId).getOrThrow()
    }
}
