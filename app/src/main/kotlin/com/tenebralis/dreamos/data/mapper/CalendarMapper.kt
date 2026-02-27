package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.UserCalendarDto
import com.tenebralis.dreamos.data.remote.dto.UserCalendarInsertDto
import com.tenebralis.dreamos.data.remote.dto.UserCalendarUpdateDto
import com.tenebralis.dreamos.domain.model.UserCalendarEvent

fun UserCalendarDto.toDomain() = UserCalendarEvent(
    id = id,
    userId = userId,
    title = title,
    description = description,
    startAt = startAt,
    endAt = endAt,
    allDay = allDay || endAt == null,
    timezone = timezone,
    rrule = rrule,
    location = location,
    scopeType = scopeType,
    worldId = worldId,
    saveId = saveId,
    aiVisibility = aiVisibility,
    metadataJson = metadataJson,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt
)

fun UserCalendarEvent.toInsertDto() = UserCalendarInsertDto(
    id = id,
    userId = userId,
    title = title,
    description = description,
    startAt = startAt,
    endAt = endAt,
    allDay = allDay,
    timezone = timezone,
    rrule = rrule,
    location = location,
    scopeType = scopeType,
    worldId = worldId,
    saveId = saveId,
    aiVisibility = aiVisibility
)

fun UserCalendarEvent.toUpdateDto() = UserCalendarUpdateDto(
    title = title,
    description = description,
    startAt = startAt,
    endAt = endAt,
    allDay = allDay,
    timezone = timezone,
    rrule = rrule,
    location = location,
    scopeType = scopeType,
    worldId = worldId,
    saveId = saveId,
    aiVisibility = aiVisibility
)
