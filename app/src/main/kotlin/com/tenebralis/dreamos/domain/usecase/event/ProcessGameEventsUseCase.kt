package com.tenebralis.dreamos.domain.usecase.event

import android.util.Log
import com.tenebralis.dreamos.domain.model.GameEvent
import com.tenebralis.dreamos.domain.repository.AchievementRepository
import com.tenebralis.dreamos.domain.repository.RelationshipRepository
import com.tenebralis.dreamos.domain.repository.TaskRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * 处理 AI 回复中解析出的游戏事件。
 *
 * 每个事件独立 try-catch，失败仅 log，不阻断聊天主流程。
 */
class ProcessGameEventsUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val relationshipRepository: RelationshipRepository,
    private val achievementRepository: AchievementRepository
) {

    suspend operator fun invoke(events: List<GameEvent>) {
        if (events.isEmpty()) return

        Log.d(TAG, "处理 ${events.size} 个游戏事件")

        for (event in events) {
            runCatching {
                when (event) {
                    is GameEvent.TaskProgress -> handleTaskProgress(event)
                    is GameEvent.AffinityChange -> handleAffinityChange(event)
                    is GameEvent.AchievementUnlock -> handleAchievementUnlock(event)
                }
            }.onFailure { e ->
                Log.e(TAG, "处理事件失败: $event — ${e.message}", e)
            }
        }
    }

    private suspend fun handleTaskProgress(event: GameEvent.TaskProgress) {
        // 查找用户在该任务上的 user_task 记录
        val userTasks = taskRepository.getUserTasks(
            status = com.tenebralis.dreamos.domain.model.enums.TaskStatus.IN_PROGRESS
        ).first().getOrNull() ?: return

        val target = userTasks.find { it.taskId == event.taskId } ?: run {
            Log.w(TAG, "未找到进行中的 user_task 对应 taskId=${event.taskId}")
            return
        }

        val newProgress = (target.progressValue + event.delta).coerceIn(0.0, 1.0)
        taskRepository.updateProgress(target.id, newProgress).getOrThrow()
        Log.d(TAG, "任务进度更新: taskId=${event.taskId}, 进度 ${target.progressValue} → $newProgress")
    }

    private suspend fun handleAffinityChange(event: GameEvent.AffinityChange) {
        val relationship = relationshipRepository.getOrCreateRelationship(
            worldId = event.worldId,
            npcId = event.npcId
        ).getOrThrow()
        relationshipRepository.adjustAffinity(
            relationshipId = relationship.id,
            delta = event.delta
        ).getOrThrow()
        Log.d(TAG, "好感度变化: npcId=${event.npcId}, delta=${event.delta}")
    }

    private suspend fun handleAchievementUnlock(event: GameEvent.AchievementUnlock) {
        achievementRepository.unlockAchievement(event.achievementId).getOrThrow()
        Log.d(TAG, "成就解锁: achievementId=${event.achievementId}")
    }

    private companion object {
        const val TAG = "ProcessGameEvents"
    }
}
