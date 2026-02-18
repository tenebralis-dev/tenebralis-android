package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.Task
import com.tenebralis.dreamos.domain.model.UserTask
import com.tenebralis.dreamos.domain.model.enums.TaskStatus
import kotlinx.coroutines.flow.Flow

/**
 * 任务仓库接口
 *
 * 对应表：tasks + user_tasks
 */
interface TaskRepository {

    /** 获取任务定义列表（可按世界筛选） */
    fun getTasks(worldId: String? = null): Flow<Result<List<Task>>>

    /** 获取当前用户的任务进度列表（可按状态筛选） */
    fun getUserTasks(status: TaskStatus? = null): Flow<Result<List<UserTask>>>

    /** 创建新任务定义 */
    suspend fun createTask(task: Task): Result<Task>

    /** 开始一个任务（创建 user_task 记录） */
    suspend fun startTask(taskId: String, saveId: String?): Result<UserTask>

    /** 更新任务进度 */
    suspend fun updateProgress(userTaskId: String, progressValue: Double): Result<UserTask>

    /** 完成任务 */
    suspend fun completeTask(userTaskId: String): Result<UserTask>

    /** 删除任务定义 */
    suspend fun deleteTask(taskId: String): Result<Unit>
}
