package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

/**
 * world_lore_entries 表 DTO
 *
 * 字段对照 prd/worldlorebook.md §4.3
 */
@Serializable
data class WorldLoreEntryDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("book_id") val bookId: String,
    val uid: Int = 0,
    val comment: String = "",
    val content: String = "",
    @SerialName("keys_json") val keysJson: JsonArray = JsonArray(emptyList()),
    @SerialName("secondary_keys_json") val secondaryKeysJson: JsonArray = JsonArray(emptyList()),
    val constant: Boolean = false,
    val selective: Boolean = false,
    @SerialName("selective_logic") val selectiveLogic: Int = 0,
    @SerialName("entry_order") val entryOrder: Int = 100,
    val position: Int = 0,
    val disable: Boolean = false,
    val probability: Int = 100,
    @SerialName("use_probability") val useProbability: Boolean = true,
    val depth: Int = 4,
    val role: Int? = null,
    @SerialName("entry_group") val entryGroup: String = "",
    @SerialName("group_override") val groupOverride: Boolean = false,
    @SerialName("group_weight") val groupWeight: Int = 100,
    @SerialName("scan_depth") val scanDepth: Int? = null,
    @SerialName("case_sensitive") val caseSensitive: Boolean? = null,
    @SerialName("match_whole_words") val matchWholeWords: Boolean? = null,
    val sticky: Int = 0,
    val cooldown: Int = 0,
    val delay: Int = 0,
    @SerialName("exclude_recursion") val excludeRecursion: Boolean = false,
    @SerialName("prevent_recursion") val preventRecursion: Boolean = false,
    @SerialName("use_group_scoring") val useGroupScoring: Boolean? = null,
    @SerialName("display_index") val displayIndex: Int = 0,
    @SerialName("automation_id") val automationId: String = "",
    @SerialName("extra_json") val extraJson: JsonObject = JsonObject(emptyMap()),
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
