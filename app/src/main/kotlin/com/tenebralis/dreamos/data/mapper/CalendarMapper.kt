package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.UserCalendarDto
import com.tenebralis.dreamos.domain.model.UserCalendarEvent

fun UserCalendarDto.toDomain() = UserCalendarEvent(
    id = id,
    userId = userId,
    title = title,
    description = description,
    startAt = startAt,
    endAt = endAt,
    isAllDay = isAllDay,
    repeatRule = repeatRule,
    scopeType = scopeType,
    scopeId = scopeId,
    aiVisibility = aiVisibility,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UserCalendarEvent.toDto() = UserCalendarDto(
    id = id,
    userId = userId,
    title = title,
    description = description,
    startAt = startAt,
    endAt = endAt,
    isAllDay = isAllDay,
    repeatRule = repeatRule,
    scopeType = scopeType,
    scopeId = scopeId,
    aiVisibility = aiVisibility,
    createdAt = createdAt,
    updatedAt = updatedAt
)
