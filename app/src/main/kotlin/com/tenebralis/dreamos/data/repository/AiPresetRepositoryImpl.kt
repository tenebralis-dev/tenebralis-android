package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.mapper.toDto
import com.tenebralis.dreamos.data.remote.dto.AiPresetDto
import com.tenebralis.dreamos.domain.model.AiPreset
import com.tenebralis.dreamos.domain.repository.AiPresetRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class AiPresetRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : AiPresetRepository {

    override fun getByUser(): Flow<Result<List<AiPreset>>> = flow {
        emit(
            runCatching {
                val userId = requireCurrentUserId()
                supabase.from(TABLE)
                    .select {
                        filter { eq("user_id", userId) }
                    }
                    .decodeList<AiPresetDto>()
                    .map { it.toDomain() }
                    .sortedBy { it.name.lowercase() }
            }
        )
    }.catch { emit(Result.failure(it)) }

    override suspend fun getById(presetId: String): Result<AiPreset> = runCatching {
        val userId = requireCurrentUserId()
        require(presetId.isNotBlank()) { "presetId 不能为空" }
        supabase.from(TABLE)
            .select {
                filter {
                    eq("id", presetId)
                    eq("user_id", userId)
                }
            }
            .decodeSingle<AiPresetDto>()
            .toDomain()
    }

    override suspend fun create(preset: AiPreset): Result<AiPreset> = runCatching {
        val userId = requireCurrentUserId()
        validateForWrite(preset, userId)

        supabase.from(TABLE)
            .insert(preset.toDto()) { select() }
            .decodeSingle<AiPresetDto>()
            .toDomain()
    }

    override suspend fun update(preset: AiPreset): Result<AiPreset> = runCatching {
        val userId = requireCurrentUserId()
        validateForWrite(preset, userId)

        supabase.from(TABLE)
            .update(preset.toDto()) {
                filter {
                    eq("id", preset.id)
                    eq("user_id", userId)
                }
                select()
            }
            .decodeSingle<AiPresetDto>()
            .toDomain()
    }

    override suspend fun getByName(name: String): Result<AiPreset?> = runCatching {
        val userId = requireCurrentUserId()
        val trimmedName = name.trim()
        require(trimmedName.isNotEmpty()) { "name 不能为空" }
        supabase.from(TABLE)
            .select {
                filter {
                    eq("user_id", userId)
                    eq("name", trimmedName)
                }
            }
            .decodeList<AiPresetDto>()
            .firstOrNull()
            ?.toDomain()
    }

    override suspend fun delete(presetId: String): Result<Unit> = runCatching {
        val userId = requireCurrentUserId()
        require(presetId.isNotBlank()) { "presetId 不能为空" }
        supabase.from(TABLE)
            .delete {
                filter {
                    eq("id", presetId)
                    eq("user_id", userId)
                }
            }
    }

    private fun validateForWrite(preset: AiPreset, expectedUserId: String) {
        require(preset.id.isNotBlank()) { "preset.id 不能为空" }
        require(preset.userId == expectedUserId) { "preset.userId 与当前会话不一致" }
        require(preset.name.trim().isNotEmpty()) { "preset.name 不能为空" }
    }

    private fun requireCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")
    }

    private companion object {
        const val TABLE = "ai_presets"
    }
}
