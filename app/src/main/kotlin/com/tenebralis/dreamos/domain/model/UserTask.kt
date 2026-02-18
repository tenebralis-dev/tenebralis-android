package com.tenebralis.dreamos.domain.model

import com.tenebralis.dreamos.domain.model.enums.TaskStatus

/**
 * 用户任务进度（领域模型）
 *
 * 对应表：user_tasks
 */
data class UserTask(
    val id: String,
    val userId: String,
    val taskId: String,
    val scopeType: String,
    val saveId: String? = null,
    val status: TaskStatus = TaskStatus.NOT_STARTED,
    val progressJson: String = "{}",
    val progressValue: Double = 0.0,
    val evidenceJson: String = "{}",
    val lastEvaluatedAt: String? = null,
    val completedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    /** 联查字段：关联的任务定义 */
    val task: Task? = null
)
