package com.tenebralis.dreamos.domain.model

/**
 * 角色卡解析后的领域模型（对齐 fast-tavern CharacterCard）
 *
 * 参考：prd/character-card.md §2.1
 */
data class CharacterCardData(
    val name: String,
    val description: String,
    val avatar: String,
    val messages: List<String>,
    val worldBook: WorldBookData?,
    val regexScripts: List<RegexScriptData>,
    val other: Map<String, String>,
    val chatDate: String,
    val createDate: String
)

/**
 * 角色卡附带的世界书
 *
 * 参考：prd/character-card.md §3.3
 */
data class WorldBookData(
    val name: String,
    val entries: List<WorldBookEntryData>
)

/**
 * 世界书条目
 */
data class WorldBookEntryData(
    val index: Int,
    val name: String,
    val content: String,
    val enabled: Boolean,
    val activationMode: String,
    val key: List<String>,
    val secondaryKey: List<String>,
    val selectiveLogic: String,
    val order: Int,
    val depth: Int,
    val position: String,
    val role: String?,
    val caseSensitive: Boolean?,
    val excludeRecursion: Boolean,
    val preventRecursion: Boolean,
    val probability: Int,
    val other: Map<String, String>
)

/**
 * 角色卡附带的正则脚本
 *
 * 参考：prd/character-card.md §3.4
 */
data class RegexScriptData(
    val id: String,
    val name: String,
    val enabled: Boolean,
    val findRegex: String,
    val replaceRegex: String,
    val trimRegex: List<String>,
    val targets: List<String>,
    val view: List<String>,
    val runOnEdit: Boolean,
    val macroMode: String,
    val minDepth: Int?,
    val maxDepth: Int?
)
