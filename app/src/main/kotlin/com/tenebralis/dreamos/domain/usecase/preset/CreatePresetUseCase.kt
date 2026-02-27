package com.tenebralis.dreamos.domain.usecase.preset

import com.tenebralis.dreamos.domain.model.AiPreset
import com.tenebralis.dreamos.domain.repository.AiPresetRepository
import javax.inject.Inject

/**
 * 创建新的 AI Preset
 */
class CreatePresetUseCase @Inject constructor(
    private val repository: AiPresetRepository
) {
    suspend operator fun invoke(preset: AiPreset): Result<AiPreset> = runCatching {
        require(preset.name.trim().isNotEmpty()) { "Preset 名称不能为空" }
        repository.create(preset).getOrThrow()
    }
}
