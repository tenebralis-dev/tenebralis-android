package com.tenebralis.dreamos.domain.usecase.event

import android.util.Log
import com.tenebralis.dreamos.domain.model.GameEvent
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

/**
 * 从 AI 回复中解析 `[GAME_EVENT]{...}[/GAME_EVENT]` 标记。
 *
 * 返回 [ParseResult]，包含：
 * - 解析出的事件列表
 * - 清理后的纯文本（去掉事件标记，展示给用户）
 */
class GameEventParser @Inject constructor() {

    data class ParseResult(
        val events: List<GameEvent>,
        val cleanContent: String
    )

    private val eventPattern = Regex(
        """\[GAME_EVENT]\s*(\{.*?\})\s*\[/GAME_EVENT]""",
        RegexOption.DOT_MATCHES_ALL
    )

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 从 assistant 回复中提取事件并返回清理后的内容。
     */
    fun parse(assistantContent: String): ParseResult {
        val events = mutableListOf<GameEvent>()

        eventPattern.findAll(assistantContent).forEach { match ->
            runCatching {
                val jsonStr = match.groupValues[1]
                val obj = json.decodeFromString<JsonObject>(jsonStr)
                val type = obj["type"]?.jsonPrimitive?.content ?: return@forEach

                when (type) {
                    "task_progress" -> {
                        val taskId = obj["task_id"]?.jsonPrimitive?.content ?: return@forEach
                        val delta = obj["delta"]?.jsonPrimitive?.double ?: return@forEach
                        events.add(GameEvent.TaskProgress(taskId, delta))
                    }
                    "affinity_change" -> {
                        val npcId = obj["npc_id"]?.jsonPrimitive?.content ?: return@forEach
                        val worldId = obj["world_id"]?.jsonPrimitive?.content ?: return@forEach
                        val delta = obj["delta"]?.jsonPrimitive?.int ?: return@forEach
                        events.add(GameEvent.AffinityChange(npcId, worldId, delta))
                    }
                    "achievement_unlock" -> {
                        val achievementId = obj["achievement_id"]?.jsonPrimitive?.content ?: return@forEach
                        events.add(GameEvent.AchievementUnlock(achievementId))
                    }
                    else -> Log.w(TAG, "未知事件类型: $type")
                }
            }.onFailure { e ->
                Log.w(TAG, "解析事件失败: ${e.message}")
            }
        }

        // 清除事件标记后的纯文本内容
        val cleanContent = eventPattern.replace(assistantContent, "").trim()

        return ParseResult(events, cleanContent)
    }

    private companion object {
        const val TAG = "GameEventParser"
    }
}
