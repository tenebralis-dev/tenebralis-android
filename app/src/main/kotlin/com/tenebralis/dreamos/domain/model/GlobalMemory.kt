package com.tenebralis.dreamos.domain.model

import com.tenebralis.dreamos.domain.model.enums.AiVisibility
import com.tenebralis.dreamos.domain.model.enums.MemorySourceType
import com.tenebralis.dreamos.domain.model.enums.MemoryType
import kotlinx.serialization.json.JsonObject

/**
 * 全局记忆（领域模型）
 *
 * 对应表：global_memories
 */
data class GlobalMemory(
    val id: String,
    val userId: String,
    val memoryKey: String?,
    val content: String,
    val summary: String?,
    val memoryType: MemoryType,
    val aiVisibility: AiVisibility,
    val importanceScore: Double,
    val confidenceScore: Double,
    val sourceType: MemorySourceType,
    val sourceRefJson: JsonObject,
    val tagsJson: JsonObject,
    val metadataJson: JsonObject,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val recalledCount: Int,
    val lastRecalledAt: String?,
    val expiresAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val deletedAt: String?
)
