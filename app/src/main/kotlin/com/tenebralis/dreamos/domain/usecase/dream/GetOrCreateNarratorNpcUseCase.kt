package com.tenebralis.dreamos.domain.usecase.dream

import com.tenebralis.dreamos.domain.model.Npc
import com.tenebralis.dreamos.domain.repository.AuthRepository
import com.tenebralis.dreamos.domain.repository.NpcRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

/**
 * 获取或创建默认叙事者 NPC。
 *
 * 在用户的 NPC 列表中查找名为 [NARRATOR_NAME] 的 NPC；
 * 若不存在，则自动创建一个带有默认叙事系统 Prompt 的 NPC。
 */
class GetOrCreateNarratorNpcUseCase @Inject constructor(
    private val npcRepository: NpcRepository,
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(): Result<Npc> = runCatching {
        val userId = authRepository.getCurrentUserId()
            ?: throw IllegalStateException("当前未登录，无法获取叙事者 NPC")

        // 查找已有叙事者
        val existingNpcs = npcRepository.getByUser().first().getOrThrow()
        val narrator = existingNpcs.firstOrNull { it.name == NARRATOR_NAME }
        if (narrator != null) return@runCatching narrator

        // 自动创建
        val newNpc = Npc(
            id = java.util.UUID.randomUUID().toString(),
            userId = userId,
            name = NARRATOR_NAME,
            description = NARRATOR_DESCRIPTION,
            promptNpcText = DEFAULT_NARRATIVE_PROMPT,
            personaJson = JsonObject(emptyMap()),
            createdAt = null,
            updatedAt = null
        )
        npcRepository.create(newNpc).getOrThrow()
    }

    companion object {
        const val NARRATOR_NAME = "叙事者"
        private const val NARRATOR_DESCRIPTION = "梦境世界的叙事者（Game Master），引导你在异世界中展开冒险。"

        const val DEFAULT_NARRATIVE_PROMPT = """你是一位经验丰富的 TRPG 叙事者（Game Master）。请以沉浸式的第二人称视角讲述故事。
- 描述场景、环境和氛围
- 扮演 NPC 角色并给出对话
- 根据玩家的行动推进剧情
- 适时给出选择和挑战
- 保持叙事的连贯性和世界观的一致性
请根据以下世界观和角色设定进行叙事。"""

        const val DREAM_THREAD_KEY = "dream_narrative"
    }
}
