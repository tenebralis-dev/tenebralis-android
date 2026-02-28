package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.local.db.dao.ContextSettingsDao
import com.tenebralis.dreamos.data.local.db.entity.ContextSettingsEntity
import com.tenebralis.dreamos.domain.model.ContextSettings
import com.tenebralis.dreamos.domain.repository.ContextSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

class ContextSettingsRepositoryImpl @Inject constructor(
    private val dao: ContextSettingsDao
) : ContextSettingsRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun get(): ContextSettings =
        dao.get()?.toDomain() ?: ContextSettings()

    override fun getAsFlow(): Flow<ContextSettings> =
        dao.getAsFlow().map { it?.toDomain() ?: ContextSettings() }

    override suspend fun update(settings: ContextSettings) {
        dao.upsert(settings.toEntity())
    }

    // ── 映射 ──

    private fun ContextSettingsEntity.toDomain(): ContextSettings = ContextSettings(
        recentMessageCount = recentMessageCount,
        memoryTopN = memoryTopN,
        maxTokenEstimate = maxTokenEstimate,
        enabledLayers = parseEnabledLayers(enabledLayersJson),
        autoLogEnabled = autoLogEnabled,
        logRetentionDays = logRetentionDays
    )

    private fun ContextSettings.toEntity(): ContextSettingsEntity = ContextSettingsEntity(
        id = 1,
        recentMessageCount = recentMessageCount,
        memoryTopN = memoryTopN,
        maxTokenEstimate = maxTokenEstimate,
        enabledLayersJson = enabledLayers.joinToString(
            prefix = "[",
            postfix = "]",
            separator = ","
        ) { "\"$it\"" },
        autoLogEnabled = autoLogEnabled,
        logRetentionDays = logRetentionDays
    )

    private fun parseEnabledLayers(raw: String): Set<String> =
        runCatching {
            json.parseToJsonElement(raw)
                .jsonArray
                .map { it.jsonPrimitive.content }
                .toSet()
        }.getOrDefault(ContextSettings.ALL_LAYERS)
}
