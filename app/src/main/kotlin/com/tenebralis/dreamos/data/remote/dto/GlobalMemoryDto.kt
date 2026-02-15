package com.tenebralis.dreamos.data.remote.dto

import com.tenebralis.dreamos.domain.model.enums.AiVisibility
import com.tenebralis.dreamos.domain.model.enums.MemorySourceType
import com.tenebralis.dreamos.domain.model.enums.MemoryType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * global_memories 表 DTO
 *
 * 字段对照 docs/memory.md
 */
@Serializable
data class GlobalMemoryDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("memory_key") val memoryKey: String? = null,
    val content: String,
    val summary: String? = null,
    @SerialName("memory_type") val memoryType: MemoryType = MemoryType.FACT,
    @SerialName("ai_visibility") val aiVisibility: AiVisibility = AiVisibility.ASSISTANT,
    @SerialName("importance_score") val importanceScore: Double = 50.0,
    @SerialName("confidence_score") val confidenceScore: Double = 70.0,
    @SerialName("source_type") val sourceType: MemorySourceType = MemorySourceType.MANUAL,
    @SerialName("source_ref_json") val sourceRefJson: JsonObject = JsonObject(emptyMap()),
    @SerialName("tags_json") val tagsJson: JsonObject = JsonObject(emptyMap()),
    @SerialName("metadata_json") val metadataJson: JsonObject = JsonObject(emptyMap()),
    @SerialName("is_pinned") val isPinned: Boolean = false,
    @SerialName("is_archived") val isArchived: Boolean = false,
    @SerialName("recalled_count") val recalledCount: Int = 0,
    @SerialName("last_recalled_at") val lastRecalledAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("deleted_at") val deletedAt: String? = null
)
