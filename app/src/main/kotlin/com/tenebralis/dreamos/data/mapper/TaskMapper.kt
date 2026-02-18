package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.TaskDto
import com.tenebralis.dreamos.data.remote.dto.UserTaskDto
import com.tenebralis.dreamos.domain.model.Task
import com.tenebralis.dreamos.domain.model.UserTask
import com.tenebralis.dreamos.domain.model.enums.TaskStatus
import com.tenebralis.dreamos.domain.model.enums.TaskType

fun TaskDto.toDomain() = Task(
    id = id,
    userId = userId,
    worldId = worldId,
    name = name,
    description = description,
    taskType = runCatching { TaskType.valueOf(taskType.uppercase()) }.getOrDefault(TaskType.SIDE),
    scopeType = scopeType,
    promptTaskText = promptTaskText,
    criteriaJson = criteriaJson,
    rewardJson = rewardJson,
    createdSource = createdSource,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Task.toDto() = TaskDto(
    id = id,
    userId = userId,
    worldId = worldId,
    name = name,
    description = description,
    taskType = taskType.name.lowercase(),
    scopeType = scopeType,
    promptTaskText = promptTaskText,
    criteriaJson = criteriaJson,
    rewardJson = rewardJson,
    createdSource = createdSource,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UserTaskDto.toDomain(task: Task? = null) = UserTask(
    id = id,
    userId = userId,
    taskId = taskId,
    scopeType = scopeType,
    saveId = saveId,
    status = runCatching { TaskStatus.valueOf(status.uppercase()) }.getOrDefault(TaskStatus.NOT_STARTED),
    progressJson = progressJson,
    progressValue = progressValue,
    evidenceJson = evidenceJson,
    lastEvaluatedAt = lastEvaluatedAt,
    completedAt = completedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    task = task
)

fun UserTask.toDto() = UserTaskDto(
    id = id,
    userId = userId,
    taskId = taskId,
    scopeType = scopeType,
    saveId = saveId,
    status = status.name.lowercase(),
    progressJson = progressJson,
    progressValue = progressValue,
    evidenceJson = evidenceJson,
    lastEvaluatedAt = lastEvaluatedAt,
    completedAt = completedAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)
