package com.tenebralis.dreamos.domain.usecase.chat

import com.tenebralis.dreamos.data.remote.ai.ChatMessage
import com.tenebralis.dreamos.domain.model.enums.MessageRole
import com.tenebralis.dreamos.domain.repository.ConversationRepository
import com.tenebralis.dreamos.domain.repository.GlobalMemoryRepository
import com.tenebralis.dreamos.domain.repository.IdentityRepository
import com.tenebralis.dreamos.domain.repository.MessageRepository
import com.tenebralis.dreamos.domain.repository.NpcRepository
import com.tenebralis.dreamos.domain.repository.SaveStateRepository
import com.tenebralis.dreamos.domain.repository.WorldRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * 按 PRD §8 多层上下文编排用例。
 *
 * 上下文层次（缺失时静默跳过，不中断）：
 * 1. 系统 Prompt（来自 connection.systemPrompt，由调用方传入）
 * 2. 世界观 — worlds.prompt_lore_text + rules_json + ai_context_json
 * 3. 身份 — user_world_identities.prompt_identity_text + persona_json
 * 4. 存档 — world_save_states.prompt_progress_text + state_json
 * 5. NPC 设定 — npcs.prompt_npc_text + persona_json
 * 6. 关系 — user_npc_relationships（M5 实现，当前跳过）
 * 7. 全局记忆 — global_memories TopN
 * 8. 近期对话消息
 */
class BuildChatContextUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val worldRepository: WorldRepository,
    private val identityRepository: IdentityRepository,
    private val saveStateRepository: SaveStateRepository,
    private val globalMemoryRepository: GlobalMemoryRepository,
    private val npcRepository: NpcRepository
) {

    /**
     * 组装聊天上下文。
     *
     * @param conversationId 会话 ID
     * @param systemPrompt   连接配置中的系统 Prompt（可为 null）
     * @param recentMessageCount 近期消息条数上限
     * @param memoryTopN 全局记忆召回上限
     * @return 组装好的 [ChatMessage] 列表（system + 历史 user/assistant）
     */
    suspend operator fun invoke(
        conversationId: String,
        systemPrompt: String? = null,
        recentMessageCount: Int = 50,
        memoryTopN: Int = 20
    ): Result<List<ChatMessage>> = runCatching {
        val conversation = conversationRepository.getById(conversationId).getOrThrow()

        // ── 组装 system message ──
        val systemParts = mutableListOf<String>()

        // 1. 系统 Prompt
        systemPrompt?.trim()?.takeIf { it.isNotEmpty() }?.let {
            systemParts += "[系统 Prompt]\n$it"
        }

        // 2. 世界层
        appendWorldContext(conversation.saveId, systemParts)

        // 3. 身份层 + 4. 存档层
        appendIdentityAndSaveContext(conversation.saveId, systemParts)

        // 5. NPC 设定
        appendNpcContext(conversation.npcId, systemParts)

        // 6. 关系层（M5，当前跳过）

        // 7. 全局记忆
        appendMemoryContext(memoryTopN, systemParts)

        // ── 构建消息列表 ──
        val messages = mutableListOf<ChatMessage>()

        // system message
        if (systemParts.isNotEmpty()) {
            messages += ChatMessage(
                role = "system",
                content = systemParts.joinToString("\n\n")
            )
        }

        // 8. 近期对话消息
        val recentMessages = messageRepository.getByConversation(conversationId)
            .first()
            .getOrThrow()
            .sortedBy { it.seq }
            .takeLast(recentMessageCount)

        recentMessages.forEach { msg ->
            val role = when (msg.role) {
                MessageRole.USER -> "user"
                MessageRole.ASSISTANT -> "assistant"
                MessageRole.SYSTEM -> "system"
                MessageRole.TOOL -> return@forEach // 跳过 tool 消息
            }
            messages += ChatMessage(role = role, content = msg.content)
        }

        messages
    }

    // ── 各层上下文组装 ──

    private suspend fun appendWorldContext(
        saveId: String,
        parts: MutableList<String>
    ) {
        val save = runCatching {
            saveStateRepository.getById(saveId).getOrThrow()
        }.getOrNull() ?: return

        val world = runCatching {
            worldRepository.getById(save.worldId).getOrThrow()
        }.getOrNull() ?: return

        val sb = StringBuilder("[世界观]")
        world.promptLoreText?.trim()?.takeIf { it.isNotEmpty() }?.let {
            sb.append("\n").append(it)
        }
        if (world.rulesJson.isNotEmpty()) {
            sb.append("\n--- 世界规则 ---\n").append(flattenJson(world.rulesJson))
        }
        if (world.aiContextJson.isNotEmpty()) {
            sb.append("\n--- 世界上下文 ---\n").append(flattenJson(world.aiContextJson))
        }
        parts += sb.toString()
    }

    private suspend fun appendIdentityAndSaveContext(
        saveId: String,
        parts: MutableList<String>
    ) {
        val save = runCatching {
            saveStateRepository.getById(saveId).getOrThrow()
        }.getOrNull() ?: return

        // 身份层
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
            parts += sb.toString()
        }

        // 存档层
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
        parts += sb.toString()
    }

    private suspend fun appendNpcContext(
        npcId: String,
        parts: MutableList<String>
    ) {
        val npc = runCatching {
            npcRepository.getById(npcId).getOrThrow()
        }.getOrNull() ?: return

        val sb = StringBuilder("[NPC 设定]")
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
        parts += sb.toString()
    }

    private suspend fun appendMemoryContext(
        topN: Int,
        parts: MutableList<String>
    ) {
        val memories = runCatching {
            globalMemoryRepository.getForContext(topN).getOrThrow()
        }.getOrNull()

        if (memories.isNullOrEmpty()) return

        val sb = StringBuilder("[全局记忆]（共 ${memories.size} 条）")
        memories.forEachIndexed { index, memory ->
            val prefix = if (memory.isPinned) "[置顶] " else ""
            sb.append("\n${index + 1}. ${prefix}${memory.content}（重要度: ${memory.importanceScore}）")
        }
        parts += sb.toString()
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
}
