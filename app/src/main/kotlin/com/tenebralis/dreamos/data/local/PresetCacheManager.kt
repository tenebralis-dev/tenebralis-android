package com.tenebralis.dreamos.data.local

import android.content.Context
import com.tenebralis.dreamos.data.remote.dto.AiPresetDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preset 本地文件缓存管理器
 *
 * 将 Preset JSON 缓存到 app 内部存储的 presets/ 目录，
 * 实现：导入/更新时写入本地，加载时优先读本地，后台同步云端。
 *
 * 存储路径：{appInternalDir}/presets/{presetId}.json
 * 索引文件：{appInternalDir}/presets/_index.json（存储 id→name 映射和时间戳）
 */
@Singleton
class PresetCacheManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        encodeDefaults = true
    }

    private val cacheDir: File
        get() = File(context.filesDir, CACHE_DIR).also { it.mkdirs() }

    // ─── 单个 Preset 操作 ────────────────────────────────

    /**
     * 将 Preset 写入本地缓存
     */
    fun put(dto: AiPresetDto) {
        try {
            val file = File(cacheDir, "${dto.id}.json")
            file.writeText(json.encodeToString(dto))
            updateIndex(dto.id, dto.name, dto.updatedAt)
        } catch (e: Exception) {
            // 缓存写入失败不影响主流程
        }
    }

    /**
     * 从本地缓存读取单个 Preset
     */
    fun get(presetId: String): AiPresetDto? {
        return try {
            val file = File(cacheDir, "$presetId.json")
            if (file.exists()) {
                json.decodeFromString<AiPresetDto>(file.readText())
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 从本地缓存读取所有 Preset
     */
    fun getAll(): List<AiPresetDto> {
        return try {
            cacheDir.listFiles { file -> file.extension == "json" && !file.name.startsWith("_") }
                ?.mapNotNull { file ->
                    try {
                        json.decodeFromString<AiPresetDto>(file.readText())
                    } catch (e: Exception) {
                        null
                    }
                }
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 删除单个缓存
     */
    fun remove(presetId: String) {
        try {
            File(cacheDir, "$presetId.json").delete()
            removeFromIndex(presetId)
        } catch (_: Exception) {}
    }

    /**
     * 批量覆盖：用云端列表替换整个本地缓存
     */
    fun replaceAll(dtos: List<AiPresetDto>) {
        try {
            // 清空现有缓存
            cacheDir.listFiles()?.forEach { it.delete() }
            // 写入新数据
            dtos.forEach { put(it) }
        } catch (_: Exception) {}
    }

    /**
     * 缓存是否存在
     */
    fun hasCache(): Boolean {
        return try {
            cacheDir.listFiles { file -> file.extension == "json" && !file.name.startsWith("_") }
                ?.isNotEmpty() == true
        } catch (e: Exception) {
            false
        }
    }

    // ─── 索引管理 ────────────────────────────────────────

    private fun updateIndex(id: String, name: String, updatedAt: String?) {
        try {
            val indexFile = File(cacheDir, INDEX_FILE)
            val index = if (indexFile.exists()) {
                json.decodeFromString<MutableMap<String, CacheIndexEntry>>(indexFile.readText())
            } else {
                mutableMapOf()
            }
            index[id] = CacheIndexEntry(name, updatedAt ?: "")
            indexFile.writeText(json.encodeToString(index))
        } catch (_: Exception) {}
    }

    private fun removeFromIndex(id: String) {
        try {
            val indexFile = File(cacheDir, INDEX_FILE)
            if (indexFile.exists()) {
                val index = json.decodeFromString<MutableMap<String, CacheIndexEntry>>(indexFile.readText())
                index.remove(id)
                indexFile.writeText(json.encodeToString(index))
            }
        } catch (_: Exception) {}
    }

    private companion object {
        const val CACHE_DIR = "presets"
        const val INDEX_FILE = "_index.json"
    }
}

@kotlinx.serialization.Serializable
private data class CacheIndexEntry(
    val name: String,
    val updatedAt: String
)
