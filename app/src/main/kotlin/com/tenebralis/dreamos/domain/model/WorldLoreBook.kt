package com.tenebralis.dreamos.domain.model

/**
 * 世界书（领域模型）
 *
 * 对应表：world_lore_books
 */
data class WorldLoreBook(
    val id: String,
    val name: String,
    val description: String?,
    val source: WorldLoreBookSource,
    val sourceFileName: String?,
    val isEnabled: Boolean,
    val displayOrder: Int,
    val globalScanDepth: Int,
    val globalCaseSensitive: Boolean,
    val globalMatchWholeWords: Boolean,
    val tokenBudget: Int,
    val storagePath: String?,
    val entryCount: Int = 0,
    val createdAt: String?,
    val updatedAt: String?
)

/**
 * 世界书来源
 */
enum class WorldLoreBookSource {
    USER,
    CHARACTER_CARD,
    IMPORTED;

    companion object {
        fun fromString(s: String): WorldLoreBookSource =
            entries.find { it.name.lowercase() == s } ?: USER
    }
}
