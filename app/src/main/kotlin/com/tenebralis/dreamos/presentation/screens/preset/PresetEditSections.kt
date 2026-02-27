package com.tenebralis.dreamos.presentation.screens.preset

import androidx.compose.runtime.Composable

/**
 * 预设编辑器各分区的 Composable
 * 采样参数、高级设置、辅助提示词、连接信息（只读）
 */

// ─── 采样参数分区 ─────────────────────────────────────────

@Composable
fun SamplingParamsSection(
    params: SamplingParams,
    expanded: Boolean,
    onToggle: () -> Unit,
    onUpdate: (SamplingParams) -> Unit
) {
    CollapsibleSection(
        title = "采样参数",
        icon = "📊",
        expanded = expanded,
        onToggle = onToggle
    ) {
        ParamSwitchRow(
            label = "解锁上下文长度",
            checked = params.maxContextUnlocked,
            onCheckedChange = { onUpdate(params.copy(maxContextUnlocked = it)) }
        )

        ParamIntSliderRow(
            label = "上下文长度（词符数）",
            value = params.maxContext,
            onValueChange = { onUpdate(params.copy(maxContext = it)) },
            valueRange = 1024..1000000
        )

        ParamIntSliderRow(
            label = "最大回复长度（词符数）",
            value = params.maxTokens,
            onValueChange = { onUpdate(params.copy(maxTokens = it)) },
            valueRange = 64..16384
        )

        ParamIntSliderRow(
            label = "每次生成备选回复数（n）",
            value = params.n,
            onValueChange = { onUpdate(params.copy(n = it)) },
            valueRange = 1..10
        )

        ParamSwitchRow(
            label = "流式传输",
            description = "随着回复的生成，逐词逐句地显示结果。关闭后回复将在完成后一次性显示。",
            checked = params.streamOpenai,
            onCheckedChange = { onUpdate(params.copy(streamOpenai = it)) }
        )

        ParamSliderRow(
            label = "温度 Temperature",
            value = params.temperature,
            onValueChange = { onUpdate(params.copy(temperature = it)) },
            valueRange = 0f..2f
        )

        ParamSliderRow(
            label = "频率惩罚 Frequency Penalty",
            value = params.frequencyPenalty,
            onValueChange = { onUpdate(params.copy(frequencyPenalty = it)) },
            valueRange = 0f..2f
        )

        ParamSliderRow(
            label = "存在惩罚 Presence Penalty",
            value = params.presencePenalty,
            onValueChange = { onUpdate(params.copy(presencePenalty = it)) },
            valueRange = 0f..2f
        )

        ParamSliderRow(
            label = "Top P",
            value = params.topP,
            onValueChange = { onUpdate(params.copy(topP = it)) },
            valueRange = 0f..1f
        )

        ParamIntSliderRow(
            label = "Top K",
            value = params.topK,
            onValueChange = { onUpdate(params.copy(topK = it)) },
            valueRange = 0..500
        )

        ParamSliderRow(
            label = "Top A",
            value = params.topA,
            onValueChange = { onUpdate(params.copy(topA = it)) },
            valueRange = 0f..1f
        )

        ParamSliderRow(
            label = "Min P",
            value = params.minP,
            onValueChange = { onUpdate(params.copy(minP = it)) },
            valueRange = 0f..1f
        )

        ParamSliderRow(
            label = "Repetition Penalty",
            value = params.repetitionPenalty,
            onValueChange = { onUpdate(params.copy(repetitionPenalty = it)) },
            valueRange = 0.5f..2f
        )

        ParamIntSliderRow(
            label = "种子 Seed（-1 = 随机）",
            value = params.seed,
            onValueChange = { onUpdate(params.copy(seed = it)) },
            valueRange = -1..9999
        )
    }
}

// ─── 高级设置分区 ─────────────────────────────────────────

@Composable
fun AdvancedSettingsSection(
    settings: AdvancedSettings,
    expanded: Boolean,
    onToggle: () -> Unit,
    onUpdate: (AdvancedSettings) -> Unit
) {
    CollapsibleSection(
        title = "高级设置",
        icon = "⚙️",
        expanded = expanded,
        onToggle = onToggle
    ) {
        ParamDropdownRow(
            label = "角色名称行为",
            selectedValue = settings.namesBehavior.toString(),
            options = listOf(
                "0" to "默认",
                "1" to "不添加",
                "2" to "始终添加"
            ),
            onValueChange = { onUpdate(settings.copy(namesBehavior = it.toIntOrNull() ?: 0)) }
        )

        ParamTextFieldRow(
            label = "继续后缀",
            value = settings.continuePostfix,
            onValueChange = { onUpdate(settings.copy(continuePostfix = it)) },
            singleLine = true
        )

        ParamSwitchRow(
            label = "继续预填充",
            description = "继续发送的是作为助手角色的最后一条消息，而不是带有指示的系统消息。",
            checked = settings.continuePrefill,
            onCheckedChange = { onUpdate(settings.copy(continuePrefill = it)) }
        )

        ParamSwitchRow(
            label = "压缩系统消息",
            description = "将连续的系统消息合并为一条（不包括示例对话），可能会提高一些模型的连贯性。",
            checked = settings.squashSystemMessages,
            onCheckedChange = { onUpdate(settings.copy(squashSystemMessages = it)) }
        )

        ParamSwitchRow(
            label = "启用函数调用",
            description = "允许使用功能工具，可以被各种扩展利用来提供附加功能。",
            checked = settings.functionCalling,
            onCheckedChange = { onUpdate(settings.copy(functionCalling = it)) }
        )

        ParamSwitchRow(
            label = "发送内联媒体",
            description = "如果模型支持，就可以在提示词中发送媒体文件。",
            checked = settings.imageInlining,
            onCheckedChange = { onUpdate(settings.copy(imageInlining = it)) }
        )

        ParamDropdownRow(
            label = "图片画质",
            selectedValue = settings.inlineImageQuality,
            options = listOf(
                "low" to "低",
                "medium" to "中",
                "high" to "高"
            ),
            onValueChange = { onUpdate(settings.copy(inlineImageQuality = it)) }
        )

        ParamSwitchRow(
            label = "请求思维链",
            description = "允许模型返回其思维过程。此设置只影响思维链是否可见。",
            checked = settings.showThoughts,
            onCheckedChange = { onUpdate(settings.copy(showThoughts = it)) }
        )

        ParamDropdownRow(
            label = "推理强度",
            selectedValue = settings.reasoningEffort,
            options = listOf(
                "auto" to "自动",
                "low" to "低",
                "medium" to "中",
                "high" to "高"
            ),
            onValueChange = { onUpdate(settings.copy(reasoningEffort = it)) }
        )

        ParamSwitchRow(
            label = "启用网页搜索",
            checked = settings.enableWebSearch,
            onCheckedChange = { onUpdate(settings.copy(enableWebSearch = it)) }
        )
    }
}

// ─── 辅助提示词分区 ───────────────────────────────────────

@Composable
fun UtilityPromptsSection(
    prompts: UtilityPrompts,
    expanded: Boolean,
    onToggle: () -> Unit,
    onUpdate: (UtilityPrompts) -> Unit
) {
    CollapsibleSection(
        title = "辅助提示词",
        icon = "📝",
        expanded = expanded,
        onToggle = onToggle
    ) {
        ParamTextFieldRow(
            label = "模拟用户 Prompt",
            value = prompts.impersonationPrompt,
            onValueChange = { onUpdate(prompts.copy(impersonationPrompt = it)) },
            maxLines = 6
        )

        ParamTextFieldRow(
            label = "新对话 Prompt",
            value = prompts.newChatPrompt,
            onValueChange = { onUpdate(prompts.copy(newChatPrompt = it)) }
        )

        ParamTextFieldRow(
            label = "新群聊 Prompt",
            value = prompts.newGroupChatPrompt,
            onValueChange = { onUpdate(prompts.copy(newGroupChatPrompt = it)) }
        )

        ParamTextFieldRow(
            label = "对话示例 Prompt",
            value = prompts.newExampleChatPrompt,
            onValueChange = { onUpdate(prompts.copy(newExampleChatPrompt = it)) }
        )

        ParamTextFieldRow(
            label = "继续生成 Prompt",
            value = prompts.continueNudgePrompt,
            onValueChange = { onUpdate(prompts.copy(continueNudgePrompt = it)) },
            maxLines = 6
        )

        ParamTextFieldRow(
            label = "群聊引导 Prompt",
            value = prompts.groupNudgePrompt,
            onValueChange = { onUpdate(prompts.copy(groupNudgePrompt = it)) }
        )

        ParamTextFieldRow(
            label = "世界信息格式",
            value = prompts.wiFormat,
            onValueChange = { onUpdate(prompts.copy(wiFormat = it)) },
            maxLines = 4
        )

        ParamTextFieldRow(
            label = "场景格式",
            value = prompts.scenarioFormat,
            onValueChange = { onUpdate(prompts.copy(scenarioFormat = it)) }
        )

        ParamTextFieldRow(
            label = "人格格式",
            value = prompts.personalityFormat,
            onValueChange = { onUpdate(prompts.copy(personalityFormat = it)) }
        )

        ParamTextFieldRow(
            label = "空消息替代内容",
            value = prompts.sendIfEmpty,
            onValueChange = { onUpdate(prompts.copy(sendIfEmpty = it)) },
            singleLine = true
        )

        ParamTextFieldRow(
            label = "助手预填充",
            value = prompts.assistantPrefill,
            onValueChange = { onUpdate(prompts.copy(assistantPrefill = it)) },
            singleLine = true
        )

        ParamTextFieldRow(
            label = "助手模拟",
            value = prompts.assistantImpersonation,
            onValueChange = { onUpdate(prompts.copy(assistantImpersonation = it)) },
            singleLine = true
        )
    }
}

// ─── 连接信息分区（只读）──────────────────────────────────

@Composable
fun ConnectionInfoSection(
    presetJson: kotlinx.serialization.json.JsonObject,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val getStr: (String) -> String? = { key ->
        try {
            presetJson[key]?.let { element ->
                val content = element.toString().trim('"')
                content.ifBlank { null }
            }
        } catch (_: Exception) { null }
    }

    CollapsibleSection(
        title = "连接/模型信息",
        icon = "🔗",
        expanded = expanded,
        onToggle = onToggle
    ) {
        getStr("chat_completion_source")?.let { InfoRow("补全来源", it) }
        getStr("custom_model")?.let { InfoRow("Custom Model", it) }
        getStr("custom_url")?.let { InfoRow("Custom URL", it) }
        getStr("openai_model")?.let { InfoRow("OpenAI Model", it) }
        getStr("claude_model")?.let { InfoRow("Claude Model", it) }
        getStr("google_model")?.let { InfoRow("Google Model", it) }
        getStr("openrouter_model")?.let { InfoRow("OpenRouter Model", it) }
        getStr("deepseek_model")?.let { InfoRow("DeepSeek Model", it) }
        getStr("mistralai_model")?.let { InfoRow("Mistral Model", it) }
    }
}
