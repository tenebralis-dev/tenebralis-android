package com.tenebralis.dreamos.domain.model

/**
 * AI 回复中嵌入的游戏事件。
 *
 * AI 在回复末尾以 `[GAME_EVENT]{...}[/GAME_EVENT]` 格式标记事件，
 * 客户端解析后分发到对应 Repository 执行。
 */
sealed interface GameEvent {

    /**
     * 任务进度变化
     * @param taskId 对应 tasks.id
     * @param delta 进度增量（0.0 ~ 1.0）
     */
    data class TaskProgress(
        val taskId: String,
        val delta: Double
    ) : GameEvent

    /**
     * NPC 好感度变化
     * @param npcId 对应 npcs.id
     * @param worldId 对应 worlds.id
     * @param delta 好感增量（-100 ~ 100 范围内的整数）
     */
    data class AffinityChange(
        val npcId: String,
        val worldId: String,
        val delta: Int
    ) : GameEvent

    /**
     * 成就解锁触发
     * @param achievementId 对应 achievements.id
     */
    data class AchievementUnlock(
        val achievementId: String
    ) : GameEvent
}
