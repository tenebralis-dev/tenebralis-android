package com.tenebralis.dreamos.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 上下文日志 Entity
 *
 * 记录每次 AI 调用时实际发送的完整上下文快照，供事后查看和调试。
 * 仅存本地 Room，不上传 Supabase。
 */
@Entity(tableName = "context_logs")
data class ContextLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "conversation_id")
    val conversationId: String,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "total_tokens_estimate")
    val totalTokensEstimate: Int,

    /** JSON: Map<String, ContextLayer> 各层内容 */
    @ColumnInfo(name = "layers_json")
    val layersJson: String,

    @ColumnInfo(name = "full_prompt_text")
    val fullPromptText: String
)
