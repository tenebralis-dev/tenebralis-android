package com.tenebralis.dreamos.domain.usecase.task

import com.tenebralis.dreamos.domain.model.UserTask
import com.tenebralis.dreamos.domain.repository.CurrencyRepository
import com.tenebralis.dreamos.domain.repository.TaskRepository
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.long
import kotlinx.serialization.json.jsonPrimitive

/**
 * 完成任务用例：标记任务完成 + 自动发放奖励积分。
 *
 * 奖励解析自 `task.rewardJson`，格式示例：
 * ```json
 * {"points": 50, "coins": 100}
 * ```
 * - `points` → 全局账户
 * - `coins` → 世界账户（需 worldId）
 */
class CompleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val currencyRepository: CurrencyRepository
) {

    /**
     * @return [CompleteTaskResult] 包含完成后的 UserTask 和发放的奖励金额
     */
    suspend operator fun invoke(userTaskId: String): Result<CompleteTaskResult> = runCatching {
        // 1. 标记任务完成
        val completedTask = taskRepository.completeTask(userTaskId).getOrThrow()

        // 2. 尝试解析并发放奖励
        val reward = distributeReward(completedTask)

        CompleteTaskResult(
            userTask = completedTask,
            pointsAwarded = reward.points,
            coinsAwarded = reward.coins
        )
    }

    private suspend fun distributeReward(userTask: UserTask): RewardPayout {
        val task = userTask.task ?: return RewardPayout()
        val rewardJson = task.rewardJson

        val rewards = runCatching {
            Json.decodeFromString<JsonObject>(rewardJson)
        }.getOrNull() ?: return RewardPayout()

        var points = 0L
        var coins = 0L

        // 发放全局积分
        val pointsAmount = rewards["points"]?.jsonPrimitive?.long
        if (pointsAmount != null && pointsAmount > 0) {
            runCatching {
                val account = currencyRepository.getOrCreateGlobalAccount("points").getOrThrow()
                currencyRepository.addTransaction(
                    accountId = account.id,
                    amount = pointsAmount,
                    reasonType = "task_reward",
                    reasonRef = userTask.taskId
                ).getOrThrow()
                points = pointsAmount
            }
            // 奖励发放失败不阻断任务完成
        }

        // 发放世界货币
        val coinsAmount = rewards["coins"]?.jsonPrimitive?.long
        if (coinsAmount != null && coinsAmount > 0 && task.worldId != null) {
            runCatching {
                val account = currencyRepository.getOrCreateWorldAccount(task.worldId, "coins").getOrThrow()
                currencyRepository.addTransaction(
                    accountId = account.id,
                    amount = coinsAmount,
                    reasonType = "task_reward",
                    reasonRef = userTask.taskId
                ).getOrThrow()
                coins = coinsAmount
            }
        }

        return RewardPayout(points, coins)
    }

    private data class RewardPayout(val points: Long = 0, val coins: Long = 0)
}

/**
 * 任务完成结果
 */
data class CompleteTaskResult(
    val userTask: UserTask,
    val pointsAwarded: Long = 0,
    val coinsAwarded: Long = 0
) {
    /** 是否有奖励发放 */
    val hasReward: Boolean get() = pointsAwarded > 0 || coinsAwarded > 0

    /** 奖励摘要文本（用于 Snackbar） */
    fun rewardSummary(): String = buildString {
        if (pointsAwarded > 0) append("+$pointsAwarded 积分")
        if (pointsAwarded > 0 && coinsAwarded > 0) append("  ")
        if (coinsAwarded > 0) append("+$coinsAwarded 金币")
    }
}
