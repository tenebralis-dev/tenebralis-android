package com.tenebralis.dreamos.presentation.screens.preset

/**
 * 预设编辑器的数据模型定义
 *
 * 这些结构从 presetJson: JsonObject 中解析出来，
 * 提供类型安全的编辑能力，保存时再序列化回 JsonObject。
 */

// ─── 采样参数 ─────────────────────────────────────────────

data class SamplingParams(
    val temperature: Float = 1f,
    val frequencyPenalty: Float = 0f,
    val presencePenalty: Float = 0f,
    val topP: Float = 1f,
    val topK: Int = 0,
    val topA: Float = 0f,
    val minP: Float = 0f,
    val repetitionPenalty: Float = 1f,
    val maxContext: Int = 4095,
    val maxTokens: Int = 300,
    val seed: Int = -1,
    val n: Int = 1,
    val maxContextUnlocked: Boolean = false,
    val streamOpenai: Boolean = true
)

// ─── 高级设置 ─────────────────────────────────────────────

data class AdvancedSettings(
    val namesBehavior: Int = 0,
    val continuePostfix: String = " ",
    val continuePrefill: Boolean = false,
    val squashSystemMessages: Boolean = false,
    val functionCalling: Boolean = false,
    val imageInlining: Boolean = true,
    val inlineImageQuality: String = "low",
    val showThoughts: Boolean = true,
    val reasoningEffort: String = "auto",
    val enableWebSearch: Boolean = false
)

// ─── 辅助提示词 ───────────────────────────────────────────

data class UtilityPrompts(
    val impersonationPrompt: String = "",
    val newChatPrompt: String = "",
    val newGroupChatPrompt: String = "",
    val newExampleChatPrompt: String = "",
    val continueNudgePrompt: String = "",
    val groupNudgePrompt: String = "",
    val wiFormat: String = "",
    val scenarioFormat: String = "",
    val personalityFormat: String = "",
    val sendIfEmpty: String = "",
    val assistantPrefill: String = "",
    val assistantImpersonation: String = ""
)

// ─── 可编辑 Prompt ────────────────────────────────────────

data class EditablePrompt(
    val identifier: String,
    val name: String,
    val role: String = "system",
    val content: String = "",
    val enabled: Boolean = true,
    val systemPrompt: Boolean = false,
    val marker: Boolean = false,
    val injectionPosition: Int = 0,
    val injectionDepth: Int = 4,
    val injectionOrder: Int = 100,
    val forbidOverrides: Boolean = false
)

// ─── Prompt 排列顺序 ─────────────────────────────────────

data class PromptOrderGroup(
    val characterId: Int,
    val orders: List<PromptOrderEntry>
)

data class PromptOrderEntry(
    val identifier: String,
    val enabled: Boolean,
    val displayName: String = ""
)
