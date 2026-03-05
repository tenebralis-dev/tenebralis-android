package com.tenebralis.dreamos.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * SillyTavern 兼容的 Preset 内部数据结构
 *
 * 这些结构用于在代码中操作 Preset 的 prompts 列表，
 * 而不是直接操作原始 JsonObject。
 *
 * 对齐 fast-tavern 的 PresetInfo / PromptInfo 类型。
 */
@Serializable
data class PresetData(
    val name: String = "",
    val prompts: List<PromptInfoData> = emptyList(),
    @SerialName("utilityPrompts") val utilityPrompts: UtilityPromptsData = UtilityPromptsData(),
    val other: JsonObject = JsonObject(emptyMap())
)

@Serializable
data class PromptInfoData(
    val identifier: String,
    val name: String,
    val enabled: Boolean = true,
    val role: String = "system",
    val content: String = "",
    val depth: Int = 0,
    val order: Int = 0,
    val position: String = "relative"   // "relative" | "fixed"
)

@Serializable
data class UtilityPromptsData(
    @SerialName("impersonationPrompt") val impersonationPrompt: String? = null,
    @SerialName("worldInfoFormat") val worldInfoFormat: String? = null,
    @SerialName("scenarioFormat") val scenarioFormat: String? = null,
    @SerialName("personalityFormat") val personalityFormat: String? = null,
    @SerialName("newChatPrompt") val newChatPrompt: String? = null,
    @SerialName("continueNudgePrompt") val continueNudgePrompt: String? = null,
    @SerialName("sendIfEmpty") val sendIfEmpty: String? = null
)

/**
 * 内置默认 Preset
 *
 * 效果等同于当前 BuildChatContextUseCase 的硬编码拼接行为。
 * 每个骨架块使用自定义宏（如 {{worldLore}}、{{npcSetting}}），
 * 由 BuildChatContextUseCase 内部生成对应内容。
 */
object DefaultPreset {

    private val json = Json { encodeDefaults = true }

    val DATA = PresetData(
        name = "__default__",
        prompts = listOf(
            PromptInfoData(
                identifier = "systemPrompt",
                name = "系统 Prompt",
                enabled = true,
                role = "system",
                content = "{{systemPrompt}}",
                position = "relative",
                order = 0
            ),
            PromptInfoData(
                identifier = "worldLore",
                name = "世界观",
                enabled = true,
                role = "system",
                content = "{{worldLore}}",
                position = "relative",
                order = 1
            ),
            PromptInfoData(
                identifier = "identity",
                name = "身份",
                enabled = true,
                role = "system",
                content = "{{identity}}",
                position = "relative",
                order = 2
            ),
            PromptInfoData(
                identifier = "saveProgress",
                name = "存档进度",
                enabled = true,
                role = "system",
                content = "{{saveProgress}}",
                position = "relative",
                order = 3
            ),
            // ── 角色卡兼容插槽 ──
            PromptInfoData(
                identifier = "charDescription",
                name = "角色描述",
                enabled = true,
                role = "system",
                content = "{{charDescription}}",
                position = "relative",
                order = 4
            ),
            PromptInfoData(
                identifier = "charPersonality",
                name = "角色性格",
                enabled = true,
                role = "system",
                content = "{{charPersonality}}",
                position = "relative",
                order = 5
            ),
            PromptInfoData(
                identifier = "scenario",
                name = "场景设定",
                enabled = true,
                role = "system",
                content = "{{scenario}}",
                position = "relative",
                order = 6
            ),
            PromptInfoData(
                identifier = "dialogueExamples",
                name = "对话示例",
                enabled = false,
                role = "system",
                content = "{{dialogueExamples}}",
                position = "relative",
                order = 7
            ),
            PromptInfoData(
                identifier = "charSystemPrompt",
                name = "角色系统提示词",
                enabled = true,
                role = "system",
                content = "{{charSystemPrompt}}",
                position = "relative",
                order = 8
            ),
            // ── 原有上下文层 ──
            PromptInfoData(
                identifier = "userData",
                name = "用户数据",
                enabled = true,
                role = "system",
                content = "{{userData}}",
                position = "relative",
                order = 9
            ),
            PromptInfoData(
                identifier = "memory",
                name = "全局记忆",
                enabled = true,
                role = "system",
                content = "{{memory}}",
                position = "relative",
                order = 10
            ),
            PromptInfoData(
                identifier = "activeTask",
                name = "活跃任务",
                enabled = true,
                role = "system",
                content = "{{activeTask}}",
                position = "relative",
                order = 11
            ),
            PromptInfoData(
                identifier = "chatHistory",
                name = "历史消息",
                enabled = true,
                role = "system",
                content = "",
                position = "relative",
                order = 12
            ),
            // ── 固定位置插槽 ──
            PromptInfoData(
                identifier = "postHistoryInstructions",
                name = "历史后指令",
                enabled = true,
                role = "system",
                content = "{{postHistoryInstructions}}",
                position = "fixed",
                depth = 0,
                order = 997
            ),
            PromptInfoData(
                identifier = "gameEvent",
                name = "游戏事件指令",
                enabled = true,
                role = "system",
                content = "{{gameEvent}}",
                position = "fixed",
                depth = 0,
                order = 999
            )
        )
    )

    /**
     * 将默认 Preset 序列化为 JsonObject，用于存储或比较
     */
    fun toJsonObject(): JsonObject {
        val str = json.encodeToString(DATA)
        return json.parseToJsonElement(str).jsonObject
    }
}
