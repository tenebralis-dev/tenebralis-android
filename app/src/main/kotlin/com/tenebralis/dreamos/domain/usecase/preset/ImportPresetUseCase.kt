package com.tenebralis.dreamos.domain.usecase.preset

import com.tenebralis.dreamos.domain.model.AiPreset
import com.tenebralis.dreamos.domain.repository.AiPresetRepository
import com.tenebralis.dreamos.domain.repository.AuthRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.util.UUID
import javax.inject.Inject

/**
 * 从 SillyTavern Preset JSON 导入
 *
 * 接受原始 JSON 字符串，解析为 AiPreset 并保存。
 * 支持检测同名冲突。
 */
class ImportPresetUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val repository: AiPresetRepository
) {

    private val json = Json { ignoreUnknownKeys = true }

    sealed class ImportResult {
        data class Success(val preset: AiPreset) : ImportResult()
        data class Conflict(
            val existingPreset: AiPreset,
            val newPresetName: String,
            val parsedPresetJson: JsonObject,
            val parsedRegexScriptsJson: JsonArray
        ) : ImportResult()
    }

    /**
     * 导入 SillyTavern Preset JSON
     *
     * @param jsonString 原始 JSON 字符串
     * @param nameOverride 可选的名称覆盖（不提供则从 JSON 中解析）
     */
    suspend operator fun invoke(
        jsonString: String,
        nameOverride: String? = null
    ): Result<ImportResult> = runCatching {
        val userId = authRepository.getCurrentUserId()
            ?: throw IllegalStateException("当前未登录")

        val rootElement = json.parseToJsonElement(jsonString).jsonObject

        // 提取 name
        val parsedName = nameOverride?.trim()
            ?: rootElement["name"]?.toString()?.trim('"')
            ?: "Imported Preset"
        require(parsedName.isNotEmpty()) { "Preset 名称不能为空" }

        // 提取 regexScripts（如果存在）
        val regexScripts = rootElement["regexScripts"]?.jsonArray ?: JsonArray(emptyList())

        // 构建 preset_json：去掉 regexScripts（单独存储）
        val presetJsonMap = rootElement.toMutableMap().apply {
            remove("regexScripts")
        }
        val presetJson = JsonObject(presetJsonMap)

        // 同名冲突检测
        val existing = repository.getByName(parsedName).getOrNull()
        if (existing != null) {
            return@runCatching ImportResult.Conflict(
                existingPreset = existing,
                newPresetName = parsedName,
                parsedPresetJson = presetJson,
                parsedRegexScriptsJson = regexScripts
            )
        }

        val preset = AiPreset(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = parsedName,
            presetJson = presetJson,
            regexScriptsJson = regexScripts,
            source = "sillytavern_import",
            createdAt = null,
            updatedAt = null
        )

        val created = repository.create(preset).getOrThrow()
        ImportResult.Success(created)
    }

    /**
     * 覆盖已有 Preset
     */
    suspend fun overwrite(
        existingPresetId: String,
        presetJson: JsonObject,
        regexScriptsJson: JsonArray,
        name: String
    ): Result<AiPreset> = runCatching {
        val userId = authRepository.getCurrentUserId()
            ?: throw IllegalStateException("当前未登录")

        val preset = AiPreset(
            id = existingPresetId,
            userId = userId,
            name = name,
            presetJson = presetJson,
            regexScriptsJson = regexScriptsJson,
            source = "sillytavern_import",
            createdAt = null,
            updatedAt = null
        )
        repository.update(preset).getOrThrow()
    }

    /**
     * 使用新名称导入（避免冲突）
     */
    suspend fun importWithRename(
        newName: String,
        presetJson: JsonObject,
        regexScriptsJson: JsonArray
    ): Result<AiPreset> = runCatching {
        val userId = authRepository.getCurrentUserId()
            ?: throw IllegalStateException("当前未登录")

        val trimmedName = newName.trim()
        require(trimmedName.isNotEmpty()) { "Preset 名称不能为空" }

        val preset = AiPreset(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = trimmedName,
            presetJson = presetJson,
            regexScriptsJson = regexScriptsJson,
            source = "sillytavern_import",
            createdAt = null,
            updatedAt = null
        )
        repository.create(preset).getOrThrow()
    }
}
