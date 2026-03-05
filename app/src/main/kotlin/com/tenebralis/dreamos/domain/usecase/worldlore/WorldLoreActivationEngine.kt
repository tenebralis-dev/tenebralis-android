package com.tenebralis.dreamos.domain.usecase.worldlore

import com.tenebralis.dreamos.domain.model.EntryPosition
import com.tenebralis.dreamos.domain.model.EntryRole
import com.tenebralis.dreamos.domain.model.SelectiveLogic
import com.tenebralis.dreamos.domain.model.WorldLoreBook
import com.tenebralis.dreamos.domain.model.WorldLoreEntry
import kotlin.random.Random

/**
 * 世界书关键词激活引擎
 *
 * 参照 PRD §5：负责扫描文本、匹配关键词、处理概率/粘滞/冷却/延迟/分组、裁剪 token 预算。
 * 纯逻辑类，无 DI 依赖，方便单元测试。
 */
class WorldLoreActivationEngine {

    /**
     * 执行激活流程。
     *
     * @param entries 所有候选条目（已启用世界书中未禁用的条目）
     * @param scanText 拼接的扫描文本（用户输入 + 近 N 条消息）
     * @param bookSettings bookId → 对应世界书设置（用于读取全局默认值和 tokenBudget）
     * @param activationStates entryId → 会话级粘滞/冷却/延迟状态（会被原地修改）
     * @param currentTurn 当前对话轮次
     * @return 按 order DESC 排序的已激活条目列表
     */
    fun activate(
        entries: List<WorldLoreEntry>,
        scanText: String,
        bookSettings: Map<String, WorldLoreBook>,
        activationStates: MutableMap<String, EntryActivationState>,
        currentTurn: Int
    ): List<ActivatedEntry> {

        // 1. 逐条目执行激活检查
        val candidates = mutableListOf<ActivatedEntry>()

        for (entry in entries) {
            val book = bookSettings[entry.bookId] ?: continue
            val state = activationStates.getOrPut(entry.id) { EntryActivationState(entry.id) }

            val activated = checkEntry(entry, book, scanText, state, currentTurn)
            if (activated) {
                candidates += ActivatedEntry(
                    entry = entry,
                    bookName = book.name,
                    position = entry.position,
                    depth = entry.depth,
                    role = entry.role,
                    estimatedTokens = estimateTokens(entry.content)
                )
                // 更新粘滞状态
                if (entry.sticky > 0) {
                    state.stickyRemaining = entry.sticky
                }
                state.lastActivatedTurn = currentTurn
            } else {
                // 未激活时递减粘滞计数
                if (state.stickyRemaining > 0) {
                    state.stickyRemaining--
                    // 粘滞期间仍然激活
                    candidates += ActivatedEntry(
                        entry = entry,
                        bookName = book.name,
                        position = entry.position,
                        depth = entry.depth,
                        role = entry.role,
                        estimatedTokens = estimateTokens(entry.content)
                    )
                }
            }

            // 递减冷却和延迟计数
            if (state.cooldownRemaining > 0) state.cooldownRemaining--
            if (state.delayRemaining > 0) state.delayRemaining--
        }

        // 2. 分组处理
        val afterGrouping = resolveGroups(candidates)

        // 3. Token 预算裁剪（按世界书分组）
        return trimToTokenBudget(afterGrouping, bookSettings)
    }

    // ─── 单条目激活检查 ─────────────────────────────────

    private fun checkEntry(
        entry: WorldLoreEntry,
        book: WorldLoreBook,
        scanText: String,
        state: EntryActivationState,
        currentTurn: Int
    ): Boolean {
        // 常驻条目直接激活
        if (entry.constant) return true

        // 冷却中 → 跳过
        if (state.cooldownRemaining > 0) return false

        // 延迟中 → 跳过（首次触发时设置延迟）
        if (entry.delay > 0 && state.delayRemaining > 0) return false

        // 关键词为空 → 跳过
        if (entry.keys.isEmpty()) return false

        // 解析条目级/世界书全局设置
        val caseSensitive = entry.caseSensitive ?: book.globalCaseSensitive
        val matchWholeWords = entry.matchWholeWords ?: book.globalMatchWholeWords

        // 主关键词匹配
        val primaryMatched = matchPrimaryKeys(scanText, entry.keys, caseSensitive, matchWholeWords)
        if (!primaryMatched) return false

        // 次关键词逻辑（仅 selective=true 时）
        if (entry.selective && entry.secondaryKeys.isNotEmpty()) {
            val secondaryMatched = checkSelectiveLogic(
                scanText, entry.secondaryKeys, entry.selectiveLogic,
                caseSensitive, matchWholeWords
            )
            if (!secondaryMatched) return false
        }

        // 延迟处理（首次匹配时开始计延迟）
        if (entry.delay > 0 && state.lastActivatedTurn < 0) {
            state.delayRemaining = entry.delay
            return false
        }

        // 概率检查
        if (entry.useProbability && entry.probability < 100) {
            if (Random.nextInt(100) >= entry.probability) {
                return false
            }
        }

        // 激活后设置冷却
        if (entry.cooldown > 0) {
            state.cooldownRemaining = entry.cooldown
        }

        return true
    }

    // ─── 关键词匹配 (PRD §5.2) ───────────────────────────

    /**
     * 检查文本是否匹配至少一个主关键词。
     */
    internal fun matchPrimaryKeys(
        text: String,
        keys: List<String>,
        caseSensitive: Boolean,
        matchWholeWords: Boolean
    ): Boolean {
        if (keys.isEmpty()) return false
        return keys.any { key ->
            matchSingleKey(text, key, caseSensitive, matchWholeWords)
        }
    }

    private fun matchSingleKey(
        text: String,
        key: String,
        caseSensitive: Boolean,
        matchWholeWords: Boolean
    ): Boolean {
        if (key.isBlank()) return false
        return if (matchWholeWords) {
            val pattern = "\\b${Regex.escape(key)}\\b"
            val options = if (caseSensitive) emptySet() else setOf(RegexOption.IGNORE_CASE)
            Regex(pattern, options).containsMatchIn(text)
        } else {
            text.contains(key, ignoreCase = !caseSensitive)
        }
    }

    // ─── 次关键词选择性逻辑 ──────────────────────────────

    private fun checkSelectiveLogic(
        text: String,
        secondaryKeys: List<String>,
        logic: SelectiveLogic,
        caseSensitive: Boolean,
        matchWholeWords: Boolean
    ): Boolean {
        val matched = secondaryKeys.map { key ->
            matchSingleKey(text, key, caseSensitive, matchWholeWords)
        }
        return when (logic) {
            SelectiveLogic.AND_ANY -> matched.any { it }
            SelectiveLogic.AND_ALL -> matched.all { it }
            SelectiveLogic.NOT_ALL -> !matched.all { it }
            SelectiveLogic.NOT_ANY -> matched.none { it }
        }
    }

    // ─── 分组处理 (PRD §5.1 步骤 5) ─────────────────────

    private fun resolveGroups(candidates: List<ActivatedEntry>): List<ActivatedEntry> {
        val (grouped, ungrouped) = candidates.partition { it.entry.group.isNotBlank() }
        if (grouped.isEmpty()) return candidates

        val result = ungrouped.toMutableList()

        // 按 group 名称分组
        val groups = grouped.groupBy { it.entry.group }
        for ((_, members) in groups) {
            val sorted = members.sortedByDescending { it.entry.order }

            // groupOverride: 组内最高 order 的覆盖其他
            val hasOverride = sorted.any { it.entry.groupOverride }
            if (hasOverride) {
                result += sorted.first()
            } else if (sorted.any { it.entry.useGroupScoring == true }) {
                // useGroupScoring: 按 groupWeight 加权随机选一个
                val totalWeight = sorted.sumOf { it.entry.groupWeight }
                if (totalWeight > 0) {
                    var roll = Random.nextInt(totalWeight)
                    for (member in sorted) {
                        roll -= member.entry.groupWeight
                        if (roll < 0) {
                            result += member
                            break
                        }
                    }
                } else {
                    result += sorted.first()
                }
            } else {
                // 默认：保留所有组内激活条目
                result += sorted
            }
        }

        return result
    }

    // ─── Token 预算裁剪 (PRD §5.1 步骤 6) ───────────────

    private fun trimToTokenBudget(
        entries: List<ActivatedEntry>,
        bookSettings: Map<String, WorldLoreBook>
    ): List<ActivatedEntry> {
        // 按 order DESC 排序（高优先级先保留）
        val sorted = entries.sortedByDescending { it.entry.order }

        // 按世界书分组计算预算
        val budgetUsed = mutableMapOf<String, Int>()
        val result = mutableListOf<ActivatedEntry>()

        for (entry in sorted) {
            val bookId = entry.entry.bookId
            val budget = bookSettings[bookId]?.tokenBudget ?: Int.MAX_VALUE
            val used = budgetUsed.getOrDefault(bookId, 0)

            if (used + entry.estimatedTokens <= budget) {
                result += entry
                budgetUsed[bookId] = used + entry.estimatedTokens
            }
        }

        return result
    }

    // ─── 工具 ─────────────────────────────────────────────

    private fun estimateTokens(text: String): Int = (text.length / 4).coerceAtLeast(1)
}

// ─── 数据类 ───────────────────────────────────────────

/**
 * 条目会话级激活状态（不持久化，随会话存在）
 */
data class EntryActivationState(
    val entryId: String,
    var stickyRemaining: Int = 0,
    var cooldownRemaining: Int = 0,
    var delayRemaining: Int = 0,
    var lastActivatedTurn: Int = -1
)

/**
 * 激活后的条目（携带世界书名称和估算 token 数）
 */
data class ActivatedEntry(
    val entry: WorldLoreEntry,
    val bookName: String,
    val position: EntryPosition,
    val depth: Int,
    val role: EntryRole?,
    val estimatedTokens: Int
)
