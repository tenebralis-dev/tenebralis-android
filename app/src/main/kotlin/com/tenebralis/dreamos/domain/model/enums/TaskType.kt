package com.tenebralis.dreamos.domain.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 任务类型（PRD §14.1 — MVP 枚举）
 *
 * 适用表：tasks.task_type
 * SQL 默认值：'side'
 */
@Serializable
enum class TaskType {
    @SerialName("main")   MAIN,
    @SerialName("side")   SIDE,
    @SerialName("daily")  DAILY,
    @SerialName("hidden") HIDDEN
}
