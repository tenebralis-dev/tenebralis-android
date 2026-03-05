package com.tenebralis.dreamos.domain.model

/**
 * 上下文设置 Domain Model
 *
 * 控制 BuildChatContextUseCase 的行为参数。
 */
data class ContextSettings(
    val recentMessageCount: Int = DEFAULT_RECENT_MESSAGE_COUNT,
    val memoryTopN: Int = DEFAULT_MEMORY_TOP_N,
    val maxTokenEstimate: Int = DEFAULT_MAX_TOKEN_ESTIMATE,
    val enabledLayers: Set<String> = ALL_LAYERS,
    val autoLogEnabled: Boolean = true,
    val logRetentionDays: Int = DEFAULT_LOG_RETENTION_DAYS
) {
    companion object {
        const val DEFAULT_RECENT_MESSAGE_COUNT = 50
        const val DEFAULT_MEMORY_TOP_N = 20
        const val DEFAULT_MAX_TOKEN_ESTIMATE = 8192
        const val DEFAULT_LOG_RETENTION_DAYS = 30

        // 所有可用层标识符
        const val LAYER_SYSTEM_PROMPT = "system_prompt"
        const val LAYER_WORLD_LORE = "world_lore"
        const val LAYER_IDENTITY = "identity"
        const val LAYER_SAVE_STATE = "save_state"
        const val LAYER_NPC_PERSONA = "npc_persona"
        const val LAYER_USER_DATA = "user_data"
        const val LAYER_MEMORIES = "memories"
        const val LAYER_ACTIVE_TASKS = "active_tasks"
        const val LAYER_GAME_EVENTS = "game_events"
        const val LAYER_WORLD_LORE_BOOK = "world_lore_book"
        const val LAYER_RECENT_MESSAGES = "recent_messages"

        val ALL_LAYERS = setOf(
            LAYER_SYSTEM_PROMPT,
            LAYER_WORLD_LORE,
            LAYER_IDENTITY,
            LAYER_SAVE_STATE,
            LAYER_NPC_PERSONA,
            LAYER_USER_DATA,
            LAYER_MEMORIES,
            LAYER_ACTIVE_TASKS,
            LAYER_GAME_EVENTS,
            LAYER_WORLD_LORE_BOOK,
            LAYER_RECENT_MESSAGES
        )

        /** 层标识符 → 显示名称 */
        val LAYER_DISPLAY_NAMES = mapOf(
            LAYER_SYSTEM_PROMPT to "系统 Prompt",
            LAYER_WORLD_LORE to "世界观",
            LAYER_IDENTITY to "身份",
            LAYER_SAVE_STATE to "存档状态",
            LAYER_NPC_PERSONA to "NPC 设定",
            LAYER_USER_DATA to "用户数据",
            LAYER_MEMORIES to "全局记忆",
            LAYER_ACTIVE_TASKS to "活跃任务",
            LAYER_GAME_EVENTS to "游戏事件指令",
            LAYER_WORLD_LORE_BOOK to "世界书",
            LAYER_RECENT_MESSAGES to "近期消息"
        )

        /** 层标识符 → 图标前缀 */
        val LAYER_ICONS = mapOf(
            LAYER_SYSTEM_PROMPT to "🔧",
            LAYER_WORLD_LORE to "🌍",
            LAYER_IDENTITY to "🎭",
            LAYER_SAVE_STATE to "💾",
            LAYER_NPC_PERSONA to "👤",
            LAYER_USER_DATA to "📝",
            LAYER_MEMORIES to "🧠",
            LAYER_ACTIVE_TASKS to "📋",
            LAYER_GAME_EVENTS to "🎮",
            LAYER_WORLD_LORE_BOOK to "📖",
            LAYER_RECENT_MESSAGES to "💬"
        )
    }
}
