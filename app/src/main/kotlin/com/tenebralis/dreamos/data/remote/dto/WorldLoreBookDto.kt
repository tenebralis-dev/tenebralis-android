package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * world_lore_books 表 DTO
 *
 * 字段对照 prd/worldlorebook.md §4.3
 */
@Serializable
data class WorldLoreBookDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val name: String,
    val description: String? = null,
    val source: String = "user",
    @SerialName("source_file_name") val sourceFileName: String? = null,
    @SerialName("is_enabled") val isEnabled: Boolean = true,
    @SerialName("display_order") val displayOrder: Int = 0,
    @SerialName("global_scan_depth") val globalScanDepth: Int = 2,
    @SerialName("global_case_sensitive") val globalCaseSensitive: Boolean = false,
    @SerialName("global_match_whole_words") val globalMatchWholeWords: Boolean = false,
    @SerialName("token_budget") val tokenBudget: Int = 2048,
    @SerialName("storage_path") val storagePath: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
