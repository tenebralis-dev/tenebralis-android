package com.tenebralis.dreamos.domain.model

/**
 * 世界书条目（领域模型）
 *
 * 对应表：world_lore_entries
 */
data class WorldLoreEntry(
    val id: String,
    val bookId: String,
    val uid: Int,
    val comment: String,
    val content: String,
    val keys: List<String>,
    val secondaryKeys: List<String>,
    val constant: Boolean,
    val selective: Boolean,
    val selectiveLogic: SelectiveLogic,
    val order: Int,
    val position: EntryPosition,
    val disabled: Boolean,
    val probability: Int,
    val useProbability: Boolean,
    val depth: Int,
    val role: EntryRole?,
    val group: String,
    val groupOverride: Boolean,
    val groupWeight: Int,
    val scanDepth: Int?,
    val caseSensitive: Boolean?,
    val matchWholeWords: Boolean?,
    val sticky: Int,
    val cooldown: Int,
    val delay: Int,
    val excludeRecursion: Boolean,
    val preventRecursion: Boolean,
    val useGroupScoring: Boolean?,
    val displayIndex: Int,
    val automationId: String
)

/**
 * 次关键词选择性逻辑
 *
 * 参见 PRD §2.4
 */
enum class SelectiveLogic(val value: Int) {
    AND_ANY(0),   // 主关键词匹配 且 次关键词至少匹配一个
    AND_ALL(1),   // 主关键词匹配 且 次关键词全部匹配
    NOT_ALL(2),   // 主关键词匹配 且 次关键词不全部匹配
    NOT_ANY(3);   // 主关键词匹配 且 次关键词全部不匹配

    companion object {
        fun fromInt(v: Int): SelectiveLogic =
            entries.find { it.value == v } ?: AND_ANY
    }
}

/**
 * 条目插入位置
 *
 * 参见 PRD §2.3
 */
enum class EntryPosition(val value: Int) {
    BEFORE_MAIN(0),
    AFTER_MAIN(1),
    BEFORE_CHAR_DEF(2),
    AFTER_CHAR_DEF(3),
    AS_MESSAGE(4),
    AFTER_EXAMPLES(5),
    AN_TOP(6),
    AN_BOTTOM(7);

    companion object {
        fun fromInt(v: Int): EntryPosition =
            entries.find { it.value == v } ?: BEFORE_MAIN
    }
}

/**
 * 注入消息角色
 */
enum class EntryRole(val value: Int) {
    SYSTEM(0),
    USER(1),
    ASSISTANT(2);

    companion object {
        fun fromInt(v: Int): EntryRole? =
            entries.find { it.value == v }
    }
}
