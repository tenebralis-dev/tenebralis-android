package com.tenebralis.dreamos.domain.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 用户任务状态（PRD §14.1 — MVP 枚举）
 *
 * 适用表：user_tasks.status
 * SQL 默认值：'not_started'
 */
@Serializable
enum class TaskStatus {
    @SerialName("not_started") NOT_STARTED,
    @SerialName("in_progress") IN_PROGRESS,
    @SerialName("completed")   COMPLETED,
    @SerialName("failed")      FAILED
}
