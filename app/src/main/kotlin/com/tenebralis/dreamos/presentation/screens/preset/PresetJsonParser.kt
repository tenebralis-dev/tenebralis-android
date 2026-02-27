package com.tenebralis.dreamos.presentation.screens.preset

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.float
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * 预设 JSON 解析器
 *
 * 负责将原始 JsonObject ↔ 可编辑的 UI 数据结构之间的双向转换。
 * buildJsonObject 保留未被编辑器管理的原始字段，避免数据丢失。
 */
object PresetJsonParser {

    // ─── 解析 ──────────────────────────────────────────────

    fun parseSamplingParams(json: JsonObject): SamplingParams = SamplingParams(
        temperature = json.floatVal("temperature", 1f),
        frequencyPenalty = json.floatVal("frequency_penalty", 0f),
        presencePenalty = json.floatVal("presence_penalty", 0f),
        topP = json.floatVal("top_p", 1f),
        topK = json.intVal("top_k", 0),
        topA = json.floatVal("top_a", 0f),
        minP = json.floatVal("min_p", 0f),
        repetitionPenalty = json.floatVal("repetition_penalty", 1f),
        maxContext = json.intVal("openai_max_context", 4095),
        maxTokens = json.intVal("openai_max_tokens", 300),
        seed = json.intVal("seed", -1),
        n = json.intVal("n", 1),
        maxContextUnlocked = json.boolVal("max_context_unlocked", false),
        streamOpenai = json.boolVal("stream_openai", true)
    )

    fun parseAdvancedSettings(json: JsonObject): AdvancedSettings = AdvancedSettings(
        namesBehavior = json.intVal("names_behavior", 0),
        continuePostfix = json.strVal("continue_postfix", " "),
        continuePrefill = json.boolVal("continue_prefill", false),
        squashSystemMessages = json.boolVal("squash_system_messages", false),
        functionCalling = json.boolVal("function_calling", false),
        imageInlining = json.boolVal("image_inlining", true),
        inlineImageQuality = json.strVal("inline_image_quality", "low"),
        showThoughts = json.boolVal("show_thoughts", true),
        reasoningEffort = json.strVal("reasoning_effort", "auto"),
        enableWebSearch = json.boolVal("enable_web_search", false)
    )

    fun parseUtilityPrompts(json: JsonObject): UtilityPrompts = UtilityPrompts(
        impersonationPrompt = json.strVal("impersonation_prompt", ""),
        newChatPrompt = json.strVal("new_chat_prompt", ""),
        newGroupChatPrompt = json.strVal("new_group_chat_prompt", ""),
        newExampleChatPrompt = json.strVal("new_example_chat_prompt", ""),
        continueNudgePrompt = json.strVal("continue_nudge_prompt", ""),
        groupNudgePrompt = json.strVal("group_nudge_prompt", ""),
        wiFormat = json.strVal("wi_format", ""),
        scenarioFormat = json.strVal("scenario_format", ""),
        personalityFormat = json.strVal("personality_format", ""),
        sendIfEmpty = json.strVal("send_if_empty", ""),
        assistantPrefill = json.strVal("assistant_prefill", ""),
        assistantImpersonation = json.strVal("assistant_impersonation", "")
    )

    fun parsePrompts(json: JsonObject): List<EditablePrompt> {
        val promptsArray = json["prompts"]?.jsonArray ?: return emptyList()
        return promptsArray.mapNotNull { element ->
            try {
                val obj = element.jsonObject
                EditablePrompt(
                    identifier = obj.strVal("identifier", ""),
                    name = obj.strVal("name", ""),
                    role = obj.strVal("role", "system"),
                    content = obj.strVal("content", ""),
                    enabled = obj.boolVal("enabled", true),
                    systemPrompt = obj.boolVal("system_prompt", false),
                    marker = obj.boolVal("marker", false),
                    injectionPosition = obj.intVal("injection_position", 0),
                    injectionDepth = obj.intVal("injection_depth", 4),
                    injectionOrder = obj.intVal("injection_order", 100),
                    forbidOverrides = obj.boolVal("forbid_overrides", false)
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    fun parsePromptOrders(
        json: JsonObject,
        prompts: List<EditablePrompt>
    ): List<PromptOrderGroup> {
        val ordersArray = json["prompt_order"]?.jsonArray ?: return emptyList()
        val nameMap = prompts.associate { it.identifier to it.name }

        return ordersArray.mapNotNull { element ->
            try {
                val obj = element.jsonObject
                val charId = obj.intVal("character_id", 0)
                val orderArr = obj["order"]?.jsonArray ?: return@mapNotNull null
                val entries = orderArr.mapNotNull { entry ->
                    try {
                        val entryObj = entry.jsonObject
                        val id = entryObj.strVal("identifier", "")
                        PromptOrderEntry(
                            identifier = id,
                            enabled = entryObj.boolVal("enabled", true),
                            displayName = nameMap[id] ?: id
                        )
                    } catch (_: Exception) { null }
                }
                PromptOrderGroup(characterId = charId, orders = entries)
            } catch (_: Exception) { null }
        }
    }

    // ─── 重建 ──────────────────────────────────────────────

    /**
     * 将编辑后的数据结构重新合并到原始 JsonObject 中。
     * 未被编辑器管理的 key 保持原样。
     */
    fun buildJsonObject(
        original: JsonObject,
        sampling: SamplingParams,
        advanced: AdvancedSettings,
        utility: UtilityPrompts,
        prompts: List<EditablePrompt>,
        promptOrders: List<PromptOrderGroup>
    ): JsonObject {
        val map = original.toMutableMap()

        // 采样参数
        map["temperature"] = JsonPrimitive(sampling.temperature)
        map["frequency_penalty"] = JsonPrimitive(sampling.frequencyPenalty)
        map["presence_penalty"] = JsonPrimitive(sampling.presencePenalty)
        map["top_p"] = JsonPrimitive(sampling.topP)
        map["top_k"] = JsonPrimitive(sampling.topK)
        map["top_a"] = JsonPrimitive(sampling.topA)
        map["min_p"] = JsonPrimitive(sampling.minP)
        map["repetition_penalty"] = JsonPrimitive(sampling.repetitionPenalty)
        map["openai_max_context"] = JsonPrimitive(sampling.maxContext)
        map["openai_max_tokens"] = JsonPrimitive(sampling.maxTokens)
        map["seed"] = JsonPrimitive(sampling.seed)
        map["n"] = JsonPrimitive(sampling.n)
        map["max_context_unlocked"] = JsonPrimitive(sampling.maxContextUnlocked)
        map["stream_openai"] = JsonPrimitive(sampling.streamOpenai)

        // 高级设置
        map["names_behavior"] = JsonPrimitive(advanced.namesBehavior)
        map["continue_postfix"] = JsonPrimitive(advanced.continuePostfix)
        map["continue_prefill"] = JsonPrimitive(advanced.continuePrefill)
        map["squash_system_messages"] = JsonPrimitive(advanced.squashSystemMessages)
        map["function_calling"] = JsonPrimitive(advanced.functionCalling)
        map["image_inlining"] = JsonPrimitive(advanced.imageInlining)
        map["inline_image_quality"] = JsonPrimitive(advanced.inlineImageQuality)
        map["show_thoughts"] = JsonPrimitive(advanced.showThoughts)
        map["reasoning_effort"] = JsonPrimitive(advanced.reasoningEffort)
        map["enable_web_search"] = JsonPrimitive(advanced.enableWebSearch)

        // 辅助提示词
        map["impersonation_prompt"] = JsonPrimitive(utility.impersonationPrompt)
        map["new_chat_prompt"] = JsonPrimitive(utility.newChatPrompt)
        map["new_group_chat_prompt"] = JsonPrimitive(utility.newGroupChatPrompt)
        map["new_example_chat_prompt"] = JsonPrimitive(utility.newExampleChatPrompt)
        map["continue_nudge_prompt"] = JsonPrimitive(utility.continueNudgePrompt)
        map["group_nudge_prompt"] = JsonPrimitive(utility.groupNudgePrompt)
        map["wi_format"] = JsonPrimitive(utility.wiFormat)
        map["scenario_format"] = JsonPrimitive(utility.scenarioFormat)
        map["personality_format"] = JsonPrimitive(utility.personalityFormat)
        map["send_if_empty"] = JsonPrimitive(utility.sendIfEmpty)
        map["assistant_prefill"] = JsonPrimitive(utility.assistantPrefill)
        map["assistant_impersonation"] = JsonPrimitive(utility.assistantImpersonation)

        // Prompts 列表
        map["prompts"] = buildPromptsArray(prompts)

        // Prompt 排列顺序
        map["prompt_order"] = buildPromptOrderArray(promptOrders)

        return JsonObject(map)
    }

    private fun buildPromptsArray(prompts: List<EditablePrompt>): JsonArray {
        return JsonArray(prompts.map { p ->
            val m = mutableMapOf<String, JsonElement>(
                "identifier" to JsonPrimitive(p.identifier),
                "name" to JsonPrimitive(p.name)
            )
            if (p.marker) {
                m["system_prompt"] = JsonPrimitive(p.systemPrompt)
                m["marker"] = JsonPrimitive(true)
            } else {
                m["role"] = JsonPrimitive(p.role)
                m["content"] = JsonPrimitive(p.content)
                m["system_prompt"] = JsonPrimitive(p.systemPrompt)
                m["marker"] = JsonPrimitive(false)
                if (!p.systemPrompt) {
                    m["enabled"] = JsonPrimitive(p.enabled)
                }
                m["injection_position"] = JsonPrimitive(p.injectionPosition)
                m["injection_depth"] = JsonPrimitive(p.injectionDepth)
                m["forbid_overrides"] = JsonPrimitive(p.forbidOverrides)
                m["injection_order"] = JsonPrimitive(p.injectionOrder)
                m["injection_trigger"] = JsonArray(emptyList())
            }
            JsonObject(m)
        })
    }

    private fun buildPromptOrderArray(orders: List<PromptOrderGroup>): JsonArray {
        return JsonArray(orders.map { group ->
            JsonObject(mapOf(
                "character_id" to JsonPrimitive(group.characterId),
                "order" to JsonArray(group.orders.map { entry ->
                    JsonObject(mapOf(
                        "identifier" to JsonPrimitive(entry.identifier),
                        "enabled" to JsonPrimitive(entry.enabled)
                    ))
                })
            ))
        })
    }

    // ─── 工具方法 ──────────────────────────────────────────

    private fun JsonObject.floatVal(key: String, default: Float): Float =
        this[key]?.jsonPrimitive?.floatOrNull ?: default

    private fun JsonObject.intVal(key: String, default: Int): Int =
        this[key]?.jsonPrimitive?.intOrNull ?: default

    private fun JsonObject.boolVal(key: String, default: Boolean): Boolean =
        this[key]?.jsonPrimitive?.booleanOrNull ?: default

    private fun JsonObject.strVal(key: String, default: String): String =
        try { this[key]?.jsonPrimitive?.content ?: default } catch (_: Exception) { default }
}
