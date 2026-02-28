package com.tenebralis.dreamos.domain.usecase.chat

import com.tenebralis.dreamos.data.remote.ai.ChatMessage
import com.tenebralis.dreamos.domain.model.ContextLayer
import com.tenebralis.dreamos.domain.model.ContextSettings
import com.tenebralis.dreamos.domain.model.enums.AiVisibility
import com.tenebralis.dreamos.domain.model.enums.MessageRole
import com.tenebralis.dreamos.domain.model.enums.TaskStatus
import com.tenebralis.dreamos.domain.repository.CalendarRepository
import com.tenebralis.dreamos.domain.repository.ContextSettingsRepository
import com.tenebralis.dreamos.domain.repository.ConversationRepository
import com.tenebralis.dreamos.domain.repository.GlobalMemoryRepository
import com.tenebralis.dreamos.domain.repository.IdentityRepository
import com.tenebralis.dreamos.domain.repository.MessageRepository
import com.tenebralis.dreamos.domain.repository.NoteRepository
import com.tenebralis.dreamos.domain.repository.NpcRepository
import com.tenebralis.dreamos.domain.repository.PomodoroRepository
import com.tenebralis.dreamos.domain.repository.SaveStateRepository
import com.tenebralis.dreamos.domain.repository.TaskRepository
import com.tenebralis.dreamos.domain.repository.WorldRepository
import com.tenebralis.dreamos.domain.usecase.context.SaveContextLogUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * 按 PRD §8 多层上下文编排用例。
 *
 * 上下文层次（缺失时静默跳过，不中断）：
 * 1. 世界观 — worlds.prompt_lore_text + rules_json + ai_context_json
 * 2. 身份 — user_world_identities.prompt_identity_text + persona_json
 * 3. 存档 — world_save_states.prompt_progress_text + state_json
 * 4. NPC 设定 — npcs.prompt_npc_text + persona_json
 * 5. 关系 — user_npc_relationships（M5 实现，当前跳过）
 * 6. 全局记忆 — global_memories TopN
 * 7. 近期对话消息
 *
 * 支持通过 [ContextSettings] 控制各层启用/禁用及参数。
 * 组装过程记录各层内容到 [lastLayers]，供日志系统使用。
 */
class BuildChatContextUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val worldRepository: WorldRepository,
    private val identityRepository: IdentityRepository,
    private val saveStateRepository: SaveStateRepository,
    private val globalMemoryRepository: GlobalMemoryRepository,
    private val npcRepository: NpcRepository,
    private val noteRepository: NoteRepository,
    private val calendarRepository: CalendarRepository,
    private val pomodoroRepository: PomodoroRepository,
    private val taskRepository: TaskRepository,
    private val contextSettingsRepository: ContextSettingsRepository
) {

    /**
     * 最近一次组装的各层信息，供 [SaveContextLogUseCase] 读取。
     * 每次 [invoke] 后更新。
     */
    var lastLayers: Map<String, ContextLayer> = emptyMap()
        private set

    /**
     * 组装聊天上下文。
     *
     * @param conversationId 会话 ID
     * @param recentMessageCount 近期消息条数上限（若为 null 则从 settings 读取）
     * @param memoryTopN 全局记忆召回上限（若为 null 则从 settings 读取）
     * @return 组装好的 [ChatMessage] 列表（system + 历史 user/assistant）
     */
    suspend operator fun invoke(
        conversationId: String,
        recentMessageCount: Int? = null,
        memoryTopN: Int? = null
    ): Result<List<ChatMessage>> = runCatching {
        val settings = contextSettingsRepository.get()
        val effectiveRecentCount = recentMessageCount ?: settings.recentMessageCount
        val effectiveMemoryTopN = memoryTopN ?: settings.memoryTopN
        val enabledLayers = settings.enabledLayers

        val conversation = conversationRepository.getById(conversationId).getOrThrow()
        val layersMap = mutableMapOf<String, ContextLayer>()

        // ── 组装 system message ──
        val systemParts = mutableListOf<String>()

        // 1. 世界层
        appendWorldContext(conversation.saveId, systemParts, enabledLayers, layersMap)

        // 3. 身份层 + 4. 存档层
        appendIdentityAndSaveContext(conversation.saveId, systemParts, enabledLayers, layersMap)

        // 5. NPC 设定
        appendNpcContext(conversation.npcId, systemParts, enabledLayers, layersMap)

        // 6. 关系层（M5，当前跳过）

        // 6.5 用户个人数据（M6: 备忘 / 日历 / 番茄钟）
        appendUserDataContext(systemParts, enabledLayers, layersMap)

        // 6.6 当前活跃任务（M7+）
        appendActiveTaskContext(systemParts, enabledLayers, layersMap)

        // 7. 全局记忆
        appendMemoryContext(effectiveMemoryTopN, systemParts, enabledLayers, layersMap)

        // 7.5 AI 事件指令格式（M7+）
        appendGameEventInstruction(systemParts, enabledLayers, layersMap)

        // ── 构建消息列表 ──
        val messages = mutableListOf<ChatMessage>()

        // system message
        if (systemParts.isNotEmpty()) {
            val systemContent = systemParts.joinToString("\n\n")
            messages += ChatMessage(role = "system", content = systemContent)

            // 记录 system_prompt 层
            if (ContextSettings.LAYER_SYSTEM_PROMPT in enabledLayers) {
                layersMap[ContextSettings.LAYER_SYSTEM_PROMPT] = ContextLayer(
                    enabled = true,
                    content = systemContent,
                    tokens = estimateTokens(systemContent)
                )
            }
        }

        // 8. 近期对话消息
        val recentMessages = messageRepository.getByConversation(conversationId)
            .first()
            .getOrThrow()
            .sortedBy { it.seq }
            .takeLast(effectiveRecentCount)

        recentMessages.forEach { msg ->
            val role = when (msg.role) {
                MessageRole.USER -> "user"
                MessageRole.ASSISTANT -> "assistant"
                MessageRole.SYSTEM -> "system"
                MessageRole.TOOL -> return@forEach // 跳过 tool 消息
            }
            messages += ChatMessage(role = role, content = msg.content)
        }

        // 记录 recent_messages 层
        val recentContent = recentMessages.joinToString("\n") { "[${it.role}] ${it.content}" }
        layersMap[ContextSettings.LAYER_RECENT_MESSAGES] = ContextLayer(
            enabled = ContextSettings.LAYER_RECENT_MESSAGES in enabledLayers,
            content = recentContent,
            tokens = estimateTokens(recentContent),
            count = recentMessages.size
        )

        lastLayers = layersMap
        messages
    }

    // ── 各层上下文组装 ──

    private suspend fun appendWorldContext(
        saveId: String,
        parts: MutableList<String>,
        enabledLayers: Set<String>,
        layersMap: MutableMap<String, ContextLayer>
    ) {
        if (ContextSettings.LAYER_WORLD_LORE !in enabledLayers) {
            layersMap[ContextSettings.LAYER_WORLD_LORE] = ContextLayer(false, null, 0)
            return
        }

        val save = runCatching {
            saveStateRepository.getById(saveId).getOrThrow()
        }.getOrNull() ?: run {
            layersMap[ContextSettings.LAYER_WORLD_LORE] = ContextLayer(true, null, 0)
            return
        }

        val world = runCatching {
            worldRepository.getById(save.worldId).getOrThrow()
        }.getOrNull() ?: run {
            layersMap[ContextSettings.LAYER_WORLD_LORE] = ContextLayer(true, null, 0)
            return
        }

        val sb = StringBuilder("[世界观]")
        sb.append("\nworld_id: ").append(world.id)
        world.promptLoreText?.trim()?.takeIf { it.isNotEmpty() }?.let {
            sb.append("\n").append(it)
        }
        if (world.rulesJson.isNotEmpty()) {
            sb.append("\n--- 世界规则 ---\n").append(flattenJson(world.rulesJson))
        }
        if (world.aiContextJson.isNotEmpty()) {
            sb.append("\n--- 世界上下文 ---\n").append(flattenJson(world.aiContextJson))
        }
        val content = sb.toString()
        parts += content
        layersMap[ContextSettings.LAYER_WORLD_LORE] = ContextLayer(true, content, estimateTokens(content))
    }

    private suspend fun appendIdentityAndSaveContext(
        saveId: String,
        parts: MutableList<String>,
        enabledLayers: Set<String>,
        layersMap: MutableMap<String, ContextLayer>
    ) {
        val save = runCatching {
            saveStateRepository.getById(saveId).getOrThrow()
        }.getOrNull()

        // 身份层
        if (ContextSettings.LAYER_IDENTITY !in enabledLayers) {
            layersMap[ContextSettings.LAYER_IDENTITY] = ContextLayer(false, null, 0)
        } else if (save != null) {
            val identity = runCatching {
                identityRepository.getById(save.identityId).getOrThrow()
            }.getOrNull()

            if (identity != null) {
                val sb = StringBuilder("[当前身份]")
                identity.promptIdentityText?.trim()?.takeIf { it.isNotEmpty() }?.let {
                    sb.append("\n").append(it)
                }
                if (identity.personaJson.isNotEmpty()) {
                    sb.append("\n--- 角色人格 ---\n").append(flattenJson(identity.personaJson))
                }
                val content = sb.toString()
                parts += content
                layersMap[ContextSettings.LAYER_IDENTITY] = ContextLayer(true, content, estimateTokens(content))
            } else {
                layersMap[ContextSettings.LAYER_IDENTITY] = ContextLayer(true, null, 0)
            }
        } else {
            layersMap[ContextSettings.LAYER_IDENTITY] = ContextLayer(true, null, 0)
        }

        // 存档层
        if (ContextSettings.LAYER_SAVE_STATE !in enabledLayers) {
            layersMap[ContextSettings.LAYER_SAVE_STATE] = ContextLayer(false, null, 0)
        } else if (save != null) {
            val sb = StringBuilder("[当前存档进度]")
            save.promptProgressText?.trim()?.takeIf { it.isNotEmpty() }?.let {
                sb.append("\n").append(it)
            }
            if (save.stateJson.isNotEmpty()) {
                sb.append("\n--- 存档状态 ---\n").append(flattenJson(save.stateJson))
            }
            save.chapter?.trim()?.takeIf { it.isNotEmpty() }?.let {
                sb.append("\n当前章节: ").append(it)
            }
            save.stage?.trim()?.takeIf { it.isNotEmpty() }?.let {
                sb.append("\n当前阶段: ").append(it)
            }
            val content = sb.toString()
            parts += content
            layersMap[ContextSettings.LAYER_SAVE_STATE] = ContextLayer(true, content, estimateTokens(content))
        } else {
            layersMap[ContextSettings.LAYER_SAVE_STATE] = ContextLayer(true, null, 0)
        }
    }

    private suspend fun appendNpcContext(
        npcId: String,
        parts: MutableList<String>,
        enabledLayers: Set<String>,
        layersMap: MutableMap<String, ContextLayer>
    ) {
        if (ContextSettings.LAYER_NPC_PERSONA !in enabledLayers) {
            layersMap[ContextSettings.LAYER_NPC_PERSONA] = ContextLayer(false, null, 0)
            return
        }

        val npc = runCatching {
            npcRepository.getById(npcId).getOrThrow()
        }.getOrNull() ?: run {
            layersMap[ContextSettings.LAYER_NPC_PERSONA] = ContextLayer(true, null, 0)
            return
        }

        val sb = StringBuilder("[NPC 设定]")
        sb.append("\nnpc_id: ").append(npc.id)
        sb.append("\n名称: ").append(npc.name)
        npc.description?.trim()?.takeIf { it.isNotEmpty() }?.let {
            sb.append("\n简介: ").append(it)
        }
        npc.promptNpcText?.trim()?.takeIf { it.isNotEmpty() }?.let {
            sb.append("\n").append(it)
        }
        if (npc.personaJson.isNotEmpty()) {
            sb.append("\n--- NPC 人格 ---\n").append(flattenJson(npc.personaJson))
        }
        val content = sb.toString()
        parts += content
        layersMap[ContextSettings.LAYER_NPC_PERSONA] = ContextLayer(true, content, estimateTokens(content))
    }

    private suspend fun appendMemoryContext(
        topN: Int,
        parts: MutableList<String>,
        enabledLayers: Set<String>,
        layersMap: MutableMap<String, ContextLayer>
    ) {
        if (ContextSettings.LAYER_MEMORIES !in enabledLayers) {
            layersMap[ContextSettings.LAYER_MEMORIES] = ContextLayer(false, null, 0)
            return
        }

        val memories = runCatching {
            globalMemoryRepository.getForContext(topN).getOrThrow()
        }.getOrNull()

        if (memories.isNullOrEmpty()) {
            layersMap[ContextSettings.LAYER_MEMORIES] = ContextLayer(true, null, 0, 0)
            return
        }

        val sb = StringBuilder("[全局记忆]（共 ${memories.size} 条）")
        memories.forEachIndexed { index, memory ->
            val prefix = if (memory.isPinned) "[置顶] " else ""
            sb.append("\n${index + 1}. ${prefix}${memory.content}（重要度: ${memory.importanceScore}）")
        }
        val content = sb.toString()
        parts += content
        layersMap[ContextSettings.LAYER_MEMORIES] = ContextLayer(
            true, content, estimateTokens(content), memories.size
        )
    }

    // ── 用户个人数据上下文（M6）──

    private suspend fun appendUserDataContext(
        parts: MutableList<String>,
        enabledLayers: Set<String>,
        layersMap: MutableMap<String, ContextLayer>
    ) {
        if (ContextSettings.LAYER_USER_DATA !in enabledLayers) {
            layersMap[ContextSettings.LAYER_USER_DATA] = ContextLayer(false, null, 0)
            return
        }

        val visibleSet = setOf(
            AiVisibility.ASSISTANT,
            AiVisibility.WORLD_CONTEXT,
            AiVisibility.SAVE_CONTEXT
        )

        val dataParts = mutableListOf<String>()

        // 备忘
        val notes = runCatching {
            noteRepository.getForContext(visibleSet, limit = 5).getOrThrow()
        }.getOrNull()
        if (!notes.isNullOrEmpty()) {
            dataParts += "[用户备忘]\n" + notes.joinToString("\n") { "- ${it.title}: ${it.content}" }
        }

        // 日历（最近 7 天）
        val events = runCatching {
            calendarRepository.getForContext(visibleSet, limit = 5).getOrThrow()
        }.getOrNull()
        if (!events.isNullOrEmpty()) {
            dataParts += "[用户日程]\n" + events.joinToString("\n") { "- ${it.title} (${it.startAt})" }
        }

        // 番茄钟（今日）
        val sessions = runCatching {
            pomodoroRepository.getTodaySessions().first().getOrThrow()
        }.getOrNull()
        if (!sessions.isNullOrEmpty()) {
            dataParts += "[今日专注] 已完成 ${sessions.size} 个番茄钟，共 ${sessions.sumOf { it.durationMinutes }} 分钟"
        }

        if (dataParts.isEmpty()) {
            layersMap[ContextSettings.LAYER_USER_DATA] = ContextLayer(true, null, 0)
            return
        }

        parts.addAll(dataParts)
        val content = dataParts.joinToString("\n\n")
        layersMap[ContextSettings.LAYER_USER_DATA] = ContextLayer(true, content, estimateTokens(content))
    }

    // ── 工具方法 ──

    private fun flattenJson(json: JsonObject): String {
        if (json.isEmpty()) return ""
        return json.entries.joinToString("\n") { (key, value) ->
            val displayValue = when (value) {
                is JsonPrimitive -> value.content
                else -> value.toString()
            }
            "$key: $displayValue"
        }
    }

    private fun JsonObject.isNotEmpty(): Boolean = this.entries.isNotEmpty()

    private fun estimateTokens(text: String): Int = (text.length / 4).coerceAtLeast(0)

    // ── M7+ 活跃任务上下文 ──

    private suspend fun appendActiveTaskContext(
        parts: MutableList<String>,
        enabledLayers: Set<String>,
        layersMap: MutableMap<String, ContextLayer>
    ) {
        if (ContextSettings.LAYER_ACTIVE_TASKS !in enabledLayers) {
            layersMap[ContextSettings.LAYER_ACTIVE_TASKS] = ContextLayer(false, null, 0)
            return
        }

        val userTasks = runCatching {
            taskRepository.getUserTasks(TaskStatus.IN_PROGRESS).first().getOrThrow()
        }.getOrNull()

        if (userTasks.isNullOrEmpty()) {
            layersMap[ContextSettings.LAYER_ACTIVE_TASKS] = ContextLayer(true, null, 0)
            return
        }

        val sb = StringBuilder("[当前活跃任务]")
        userTasks.take(5).forEach { ut ->
            val name = ut.task?.name ?: "(未知任务)"
            val taskId = ut.taskId
            val desc = ut.task?.description?.take(60) ?: ""
            val progress = "${(ut.progressValue * 100).toInt()}%"
            sb.append("\n- $name（task_id: $taskId, 进度: $progress）")
            if (desc.isNotEmpty()) sb.append("  $desc")
        }
        val content = sb.toString()
        parts += content
        layersMap[ContextSettings.LAYER_ACTIVE_TASKS] = ContextLayer(true, content, estimateTokens(content))
    }

    private fun appendGameEventInstruction(
        parts: MutableList<String>,
        enabledLayers: Set<String>,
        layersMap: MutableMap<String, ContextLayer>
    ) {
        if (ContextSettings.LAYER_GAME_EVENTS !in enabledLayers) {
            layersMap[ContextSettings.LAYER_GAME_EVENTS] = ContextLayer(false, null, 0)
            return
        }

        val content = """
[游戏事件指令]
当你认为用户的行为应该推进任务进度或影响 NPC 好感时，在回复末尾添加事件标记（用户不可见）：
[GAME_EVENT]{"type":"task_progress","task_id":"<从[当前活跃任务]中复制task_id>","delta":0.2}[/GAME_EVENT]
[GAME_EVENT]{"type":"affinity_change","npc_id":"<从[NPC 设定]中复制npc_id>","world_id":"<从[世界观]中获取>","delta":5}[/GAME_EVENT]
规则：
- task_id / npc_id / world_id 必须使用上方上下文中提供的真实 ID，严禁编造
- delta 范围：task_progress 为 0.0~1.0，affinity_change 为 -20~20
- 仅当用户行为明确相关时才添加，不要每次都添加
- 一条回复可包含多个事件标记
- 如果上下文中没有活跃任务或 NPC 信息，则不要添加对应事件
        """.trimIndent()
        parts += content
        layersMap[ContextSettings.LAYER_GAME_EVENTS] = ContextLayer(true, content, estimateTokens(content))
    }
}
