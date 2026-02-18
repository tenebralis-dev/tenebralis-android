package com.tenebralis.dreamos.domain.model

import com.tenebralis.dreamos.domain.model.enums.TaskType

/**
 * 任务定义（领域模型）
 *
 * 对应表：tasks
 */
data class Task(
    val id: String,
    val userId: String,
    val worldId: String? = null,
    val name: String,
    val description: String? = null,
    val taskType: TaskType = TaskType.SIDE,
    val scopeType: String,
    val promptTaskText: String? = null,
    val criteriaJson: String = "{}",
    val rewardJson: String = "{}",
    val createdSource: String = "manual",
    val createdAt: String? = null,
    val updatedAt: String? = null
)
