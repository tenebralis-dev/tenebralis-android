package com.tenebralis.dreamos.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 上下文设置 Entity（单行表）
 *
 * 控制 BuildChatContextUseCase 的行为参数。
 * id 固定为 1，整表只存一行。
 */
@Entity(tableName = "context_settings")
data class ContextSettingsEntity(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "recent_message_count")
    val recentMessageCount: Int = 50,

    @ColumnInfo(name = "memory_top_n")
    val memoryTopN: Int = 20,

    @ColumnInfo(name = "max_token_estimate")
    val maxTokenEstimate: Int = 8192,

    /** JSON 数组: ["system_prompt","world_lore",...] */
    @ColumnInfo(name = "enabled_layers_json")
    val enabledLayersJson: String = DEFAULT_ENABLED_LAYERS_JSON,

    @ColumnInfo(name = "auto_log_enabled")
    val autoLogEnabled: Boolean = true,

    @ColumnInfo(name = "log_retention_days")
    val logRetentionDays: Int = 30
) {
    companion object {
        const val DEFAULT_ENABLED_LAYERS_JSON =
            """["system_prompt","world_lore","identity","save_state","npc_persona","user_data","memories","active_tasks","game_events","recent_messages"]"""
    }
}
