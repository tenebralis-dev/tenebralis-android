package com.tenebralis.dreamos.domain.model

import kotlinx.serialization.json.JsonObject

/**
 * 用户设置（领域模型）
 *
 * 对应表：user_settings
 * ui_config / systemPreferences 暂用 JsonObject，后续可拆为具体业务结构体。
 */
data class UserSettings(
    val userId: String,
    val uiConfig: JsonObject,
    val systemPreferences: JsonObject,
    val updatedAt: String?
)
