package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.AiPreset
import kotlinx.coroutines.flow.Flow

/**
 * AI Preset 仓库接口
 *
 * 对应表：ai_presets
 */
interface AiPresetRepository {

    /** 获取当前用户的所有 Preset */
    fun getByUser(): Flow<Result<List<AiPreset>>>

    /** 根据 ID 获取 Preset 详情 */
    suspend fun getById(presetId: String): Result<AiPreset>

    /** 创建新 Preset */
    suspend fun create(preset: AiPreset): Result<AiPreset>

    /** 更新 Preset */
    suspend fun update(preset: AiPreset): Result<AiPreset>

    /** 按名称查找 Preset（用于同名冲突检测） */
    suspend fun getByName(name: String): Result<AiPreset?>

    /** 删除 Preset */
    suspend fun delete(presetId: String): Result<Unit>
}
