package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.remote.dto.UserSettingsDto
import com.tenebralis.dreamos.domain.model.UserSettings

fun UserSettingsDto.toDomain() = UserSettings(
    userId = userId,
    uiConfig = uiConfig,
    systemPreferences = systemPreferences,
    updatedAt = updatedAt
)

fun UserSettings.toDto() = UserSettingsDto(
    userId = userId,
    uiConfig = uiConfig,
    systemPreferences = systemPreferences,
    updatedAt = updatedAt
)
