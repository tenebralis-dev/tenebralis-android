package com.tenebralis.dreamos.domain.usecase.preset

import com.tenebralis.dreamos.domain.model.AiPreset
import com.tenebralis.dreamos.domain.repository.AiPresetRepository
import javax.inject.Inject

/**
 * 更新 AI Preset
 */
class UpdatePresetUseCase @Inject constructor(
    private val repository: AiPresetRepository
) {
    suspend operator fun invoke(preset: AiPreset): Result<AiPreset> = runCatching {
        require(preset.id.isNotBlank()) { "preset.id 不能为空" }
        require(preset.name.trim().isNotEmpty()) { "Preset 名称不能为空" }
        repository.update(preset).getOrThrow()
    }
}
