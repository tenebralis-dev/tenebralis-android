package com.tenebralis.dreamos.domain.usecase.worldlore

import com.tenebralis.dreamos.domain.model.EntryPosition

/**
 * 将激活条目按 position 分类格式化。
 *
 * 参照 PRD §6.1：按 position 将条目内容插入到上下文对应位置。
 * Phase 2 MVP 直接拼接 content，暂不实现 preset wi_format 模板。
 */
class WorldLoreFormatter {

    fun format(
        entries: List<ActivatedEntry>,
        totalCandidateCount: Int = 0
    ): WorldLoreFormattedResult {
        if (entries.isEmpty()) {
            return WorldLoreFormattedResult(
                beforeMain = null,
                afterMain = null,
                messageEntries = emptyList(),
                totalTokens = 0,
                activatedCount = 0,
                totalCandidateCount = totalCandidateCount
            )
        }

        // 按 position 分类
        val beforePositions = setOf(
            EntryPosition.BEFORE_MAIN,
            EntryPosition.BEFORE_CHAR_DEF,
            EntryPosition.AN_TOP
        )
        val afterPositions = setOf(
            EntryPosition.AFTER_MAIN,
            EntryPosition.AFTER_CHAR_DEF,
            EntryPosition.AFTER_EXAMPLES,
            EntryPosition.AN_BOTTOM
        )

        val beforeEntries = entries.filter { it.position in beforePositions }
        val afterEntries = entries.filter { it.position in afterPositions }
        val messageEntries = entries.filter { it.position == EntryPosition.AS_MESSAGE }

        val beforeText = formatEntries(beforeEntries)
        val afterText = formatEntries(afterEntries)

        return WorldLoreFormattedResult(
            beforeMain = beforeText,
            afterMain = afterText,
            messageEntries = messageEntries,
            totalTokens = entries.sumOf { it.estimatedTokens },
            activatedCount = entries.size,
            totalCandidateCount = totalCandidateCount
        )
    }

    private fun formatEntries(entries: List<ActivatedEntry>): String? {
        if (entries.isEmpty()) return null
        return entries.joinToString("\n") { it.entry.content }
    }
}

/**
 * 格式化后的世界书激活结果
 */
data class WorldLoreFormattedResult(
    val beforeMain: String?,
    val afterMain: String?,
    val messageEntries: List<ActivatedEntry>,
    val totalTokens: Int,
    val activatedCount: Int,
    val totalCandidateCount: Int
)
