package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.PomodoroSessionDto
import com.tenebralis.dreamos.domain.model.PomodoroSession

fun PomodoroSessionDto.toDomain() = PomodoroSession(
    id = id,
    userId = userId,
    startedAt = startedAt,
    endedAt = endedAt,
    durationMinutes = durationMinutes,
    isCompleted = isCompleted,
    taskDescription = taskDescription,
    scopeType = scopeType,
    scopeId = scopeId,
    aiVisibility = aiVisibility,
    createdAt = createdAt
)

fun PomodoroSession.toDto() = PomodoroSessionDto(
    id = id,
    userId = userId,
    startedAt = startedAt,
    endedAt = endedAt,
    durationMinutes = durationMinutes,
    isCompleted = isCompleted,
    taskDescription = taskDescription,
    scopeType = scopeType,
    scopeId = scopeId,
    aiVisibility = aiVisibility,
    createdAt = createdAt
)
