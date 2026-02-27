package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.local.PresetCacheManager
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

/**
 * AiPreset 仓库实现
 *
 * 缓存策略：
 * - 读取：优先读本地缓存 → 后台从云端同步并更新缓存
 * - 写入：先写云端 → 成功后写入本地缓存（write-through）
 * - 删除：先删云端 → 成功后删本地缓存
 */
class AiPresetRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val cache: PresetCacheManager
) : AiPresetRepository {

    override fun getByUser(): Flow<Result<List<AiPreset>>> = flow {
        // 1. 优先发射本地缓存
        if (cache.hasCache()) {
            val cached = cache.getAll().map { it.toDomain() }.sortedBy { it.name.lowercase() }
            emit(Result.success(cached))
        }

        // 2. 从云端拉取最新数据
        val cloudResult = runCatching {
            val userId = requireCurrentUserId()
            supabase.from(TABLE)
                .select { filter { eq("user_id", userId) } }
                .decodeList<AiPresetDto>()
        }

        cloudResult.fold(
            onSuccess = { dtos ->
                // 用云端数据覆盖本地缓存
                cache.replaceAll(dtos)
                val list = dtos.map { it.toDomain() }.sortedBy { it.name.lowercase() }
                emit(Result.success(list))
            },
            onFailure = { e ->
                // 云端失败但有缓存 → 不发射错误（已经发射了缓存数据）
                // 云端失败且无缓存 → 发射错误
                if (!cache.hasCache()) {
                    emit(Result.failure(e))
                }
            }
        )
    }.catch { emit(Result.failure(it)) }

    override suspend fun getById(presetId: String): Result<AiPreset> = runCatching {
        require(presetId.isNotBlank()) { "presetId 不能为空" }

        // 优先读缓存
        cache.get(presetId)?.let { return@runCatching it.toDomain() }

        // 缓存未命中，从云端拉取
        val userId = requireCurrentUserId()
        val dto = supabase.from(TABLE)
            .select {
                filter {
                    eq("id", presetId)
                    eq("user_id", userId)
                }
            }
            .decodeSingle<AiPresetDto>()

        cache.put(dto)
        dto.toDomain()
    }

    override suspend fun create(preset: AiPreset): Result<AiPreset> = runCatching {
        val userId = requireCurrentUserId()
        validateForWrite(preset, userId)

        val dto = supabase.from(TABLE)
            .insert(preset.toDto()) { select() }
            .decodeSingle<AiPresetDto>()

        cache.put(dto)
        dto.toDomain()
    }

    override suspend fun update(preset: AiPreset): Result<AiPreset> = runCatching {
        val userId = requireCurrentUserId()
        validateForWrite(preset, userId)

        val dto = supabase.from(TABLE)
            .update(preset.toDto()) {
                filter {
                    eq("id", preset.id)
                    eq("user_id", userId)
                }
                select()
            }
            .decodeSingle<AiPresetDto>()

        cache.put(dto)
        dto.toDomain()
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
            ?.also { cache.put(it) }
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
        cache.remove(presetId)
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
