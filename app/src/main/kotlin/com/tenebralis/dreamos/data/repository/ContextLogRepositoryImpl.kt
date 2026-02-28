package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.local.db.dao.ContextLogDao
import com.tenebralis.dreamos.data.local.db.entity.ContextLogEntity
import com.tenebralis.dreamos.domain.model.ContextLayer
import com.tenebralis.dreamos.domain.model.ContextLog
import com.tenebralis.dreamos.domain.repository.ContextLogRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class ContextLogRepositoryImpl @Inject constructor(
    private val dao: ContextLogDao
) : ContextLogRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getAll(): Flow<List<ContextLog>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getByConversation(conversationId: String): Flow<List<ContextLog>> =
        dao.getByConversation(conversationId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): ContextLog? =
        dao.getById(id)?.toDomain()

    override suspend fun save(log: ContextLog): Long {
        val entity = ContextLogEntity(
            conversationId = log.conversationId,
            createdAt = log.createdAt,
            totalTokensEstimate = log.totalTokensEstimate,
            layersJson = layersToJson(log.layers),
            fullPromptText = log.fullPromptText
        )
        return dao.insert(entity)
    }

    override suspend fun deleteBefore(before: String): Int =
        dao.deleteBefore(before)

    override suspend fun deleteAll() =
        dao.deleteAll()

    // ── 映射 ──

    private fun ContextLogEntity.toDomain(): ContextLog = ContextLog(
        id = id,
        conversationId = conversationId,
        createdAt = createdAt,
        totalTokensEstimate = totalTokensEstimate,
        layers = jsonToLayers(layersJson),
        fullPromptText = fullPromptText
    )

    private fun layersToJson(layers: Map<String, ContextLayer>): String {
        val obj = buildJsonObject {
            layers.forEach { (key, layer) ->
                put(key, buildJsonObject {
                    put("enabled", layer.enabled)
                    put("content", layer.content ?: "")
                    put("tokens", layer.tokens)
                    layer.count?.let { put("count", it) }
                })
            }
        }
        return obj.toString()
    }

    private fun jsonToLayers(raw: String): Map<String, ContextLayer> {
        if (raw.isBlank()) return emptyMap()
        return runCatching {
            val obj = json.parseToJsonElement(raw).jsonObject
            obj.entries.associate { (key, value) ->
                val layerObj = value.jsonObject
                key to ContextLayer(
                    enabled = layerObj["enabled"]?.jsonPrimitive?.boolean ?: true,
                    content = layerObj["content"]?.jsonPrimitive?.content,
                    tokens = layerObj["tokens"]?.jsonPrimitive?.int ?: 0,
                    count = layerObj["count"]?.jsonPrimitive?.intOrNull
                )
            }
        }.getOrDefault(emptyMap())
    }
}
