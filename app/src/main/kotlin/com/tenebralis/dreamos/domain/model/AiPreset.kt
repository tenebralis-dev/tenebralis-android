package com.tenebralis.dreamos.domain.model

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

/**
 * AI Preset 定义（领域模型）
 *
 * 对应表：ai_presets
 *
 * Preset 是一套 Prompt 组装模板，定义了上下文各层的排列顺序、
 * 启用状态和内容模板。兼容 SillyTavern 的 PresetInfo 格式。
 */
data class AiPreset(
    val id: String,
    val userId: String,
    val name: String,
    /** 完整的 Preset 结构（prompts 数组 + utilityPrompts + 采样参数） */
    val presetJson: JsonObject,
    /** 预设绑定的正则脚本 */
    val regexScriptsJson: JsonArray,
    /** 来源：'manual' | 'sillytavern_import' */
    val source: String?,
    val createdAt: String?,
    val updatedAt: String?
)
