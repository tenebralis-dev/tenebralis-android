package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * user_settings 表 DTO
 *
 * ui_config 默认：{"theme_mode":"light","primary_color":"#A0C4FF","font_family":"System","wallpaper_url":null}
 * system_preferences 默认：{"language":"zh_CN","enable_notifications":true,"ai_personality_mode":"standard"}
 */
@Serializable
data class UserSettingsDto(
    @SerialName("user_id") val userId: String,
    @SerialName("ui_config") val uiConfig: JsonObject = JsonObject(emptyMap()),
    @SerialName("system_preferences") val systemPreferences: JsonObject = JsonObject(emptyMap()),
    @SerialName("updated_at") val updatedAt: String? = null
)
