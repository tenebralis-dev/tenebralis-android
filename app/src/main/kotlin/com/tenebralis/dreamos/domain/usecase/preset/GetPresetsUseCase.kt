package com.tenebralis.dreamos.domain.usecase.preset

import com.tenebralis.dreamos.domain.model.AiPreset
import com.tenebralis.dreamos.domain.repository.AiPresetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取当前用户的所有 AI Preset
 */
class GetPresetsUseCase @Inject constructor(
    private val repository: AiPresetRepository
) {
    operator fun invoke(): Flow<Result<List<AiPreset>>> = repository.getByUser()
}
