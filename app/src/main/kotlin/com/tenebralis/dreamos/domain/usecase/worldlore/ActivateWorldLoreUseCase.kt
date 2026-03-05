package com.tenebralis.dreamos.domain.usecase.worldlore

import com.tenebralis.dreamos.domain.repository.WorldLoreRepository
import javax.inject.Inject

/**
 * 世界书激活编排用例
 *
 * 参照 PRD §6.1：从 Repository 获取候选数据 → 构建扫描文本 → 调用激活引擎 → 格式化输出。
 */
class ActivateWorldLoreUseCase @Inject constructor(
    private val worldLoreRepository: WorldLoreRepository
) {
    private val engine = WorldLoreActivationEngine()
    private val formatter = WorldLoreFormatter()

    /**
     * 执行世界书激活。
     *
     * @param recentMessages 近期消息内容列表（newest last）
     * @param userInput 用户最新输入
     * @param activationStates 会话级激活状态（会被原地修改）
     * @param currentTurn 当前对话轮次
     * @return 格式化后的激活结果
     */
    suspend operator fun invoke(
        recentMessages: List<String>,
        userInput: String,
        activationStates: MutableMap<String, EntryActivationState>,
        currentTurn: Int
    ): Result<WorldLoreFormattedResult> = runCatching {

        // 1. 获取已启用的世界书
        val books = worldLoreRepository.getEnabledBooks().getOrThrow()
        if (books.isEmpty()) {
            return@runCatching formatter.format(emptyList())
        }
        val bookSettings = books.associateBy { it.id }

        // 2. 获取所有候选条目
        val allEntries = mutableListOf<com.tenebralis.dreamos.domain.model.WorldLoreEntry>()
        for (book in books) {
            val entries = worldLoreRepository.getActiveEntriesByBook(book.id).getOrThrow()
            allEntries.addAll(entries)
        }

        if (allEntries.isEmpty()) {
            return@runCatching formatter.format(emptyList(), totalCandidateCount = 0)
        }

        // 3. 构建扫描文本
        //    对每个条目，scanDepth 决定扫描多少条消息
        //    这里用全局最大 scanDepth 构建一次完整的扫描文本
        val maxScanDepth = allEntries.maxOf { entry ->
            entry.scanDepth ?: (bookSettings[entry.bookId]?.globalScanDepth ?: 2)
        }
        val messagesToScan = recentMessages.takeLast(maxScanDepth)
        val scanText = buildString {
            append(userInput)
            for (msg in messagesToScan) {
                append("\n")
                append(msg)
            }
        }

        // 4. 执行激活引擎
        val activated = engine.activate(
            entries = allEntries,
            scanText = scanText,
            bookSettings = bookSettings,
            activationStates = activationStates,
            currentTurn = currentTurn
        )

        // 5. 格式化输出
        formatter.format(activated, totalCandidateCount = allEntries.size)
    }
}
