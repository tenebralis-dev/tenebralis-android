package com.tenebralis.dreamos.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * NPC persona_json 字段的结构化数据类
 *
 * 对齐 prd/character-card.md §3.2
 */
@Serializable
data class PersonaJsonData(
    val source: String? = null,
    @SerialName("source_format_version") val sourceFormatVersion: String? = null,
    @SerialName("avatar_file") val avatarFile: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("first_message") val firstMessage: String? = null,
    @SerialName("alternate_greetings") val alternateGreetings: List<String> = emptyList(),
    val personality: String? = null,
    val scenario: String? = null,
    @SerialName("mes_example") val mesExample: String? = null,
    @SerialName("system_prompt") val systemPrompt: String? = null,
    @SerialName("post_history_instructions") val postHistoryInstructions: String? = null,
    @SerialName("creator_notes") val creatorNotes: String? = null,
    val creator: String? = null,
    @SerialName("character_version") val characterVersion: String? = null,
    val tags: List<String> = emptyList(),
    val talkativeness: String? = null,
    @SerialName("depth_prompt") val depthPrompt: DepthPromptData? = null,
    val fav: Boolean = false,
    @SerialName("character_book") val characterBook: JsonObject? = null,
    @SerialName("regex_scripts") val regexScripts: List<JsonObject> = emptyList(),
    @SerialName("raw_other") val rawOther: JsonObject? = null
)

@Serializable
data class DepthPromptData(
    val prompt: String = "",
    val depth: Int = 4,
    val role: String = "system"
)
