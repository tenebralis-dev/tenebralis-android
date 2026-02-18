package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.mapper.toDto
import com.tenebralis.dreamos.data.remote.dto.TaskDto
import com.tenebralis.dreamos.data.remote.dto.UserTaskDto
import com.tenebralis.dreamos.domain.model.Task
import com.tenebralis.dreamos.domain.model.UserTask
import com.tenebralis.dreamos.domain.model.enums.TaskStatus
import com.tenebralis.dreamos.domain.repository.TaskRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class TaskRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : TaskRepository {

    override fun getTasks(worldId: String?): Flow<Result<List<Task>>> = flow {
        emit(runCatching {
            val userId = requireCurrentUserId()
            supabase.from(TABLE_TASKS)
                .select {
                    filter {
                        eq("user_id", userId)
                        if (worldId != null) eq("world_id", worldId)
                    }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<TaskDto>()
                .map { it.toDomain() }
        })
    }.catch { emit(Result.failure(it)) }

    override fun getUserTasks(status: TaskStatus?): Flow<Result<List<UserTask>>> = flow {
        emit(runCatching {
            val userId = requireCurrentUserId()

            // 1. 查询 user_tasks
            val userTaskDtos = supabase.from(TABLE_USER_TASKS)
                .select {
                    filter {
                        eq("user_id", userId)
                        if (status != null) eq("status", status.name.lowercase())
                    }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<UserTaskDto>()

            if (userTaskDtos.isEmpty()) return@runCatching emptyList()

            // 2. 批量查询关联的 task 定义
            val taskIds = userTaskDtos.map { it.taskId }.distinct()
            val taskDtos = supabase.from(TABLE_TASKS)
                .select {
                    filter {
                        eq("user_id", userId)
                        isIn("id", taskIds)
                    }
                }
                .decodeList<TaskDto>()
            val taskMap = taskDtos.associate { it.id to it.toDomain() }

            // 3. 组装
            userTaskDtos.map { ut -> ut.toDomain(task = taskMap[ut.taskId]) }
        })
    }.catch { emit(Result.failure(it)) }

    override suspend fun createTask(task: Task): Result<Task> = runCatching {
        val userId = requireCurrentUserId()
        require(task.userId == userId) { "task.userId 与当前会话不一致" }
        require(task.name.trim().isNotEmpty()) { "任务名称不能为空" }

        supabase.from(TABLE_TASKS)
            .insert(task.toDto()) { select() }
            .decodeSingle<TaskDto>()
            .toDomain()
    }

    override suspend fun startTask(taskId: String, saveId: String?): Result<UserTask> =
        runCatching {
            val userId = requireCurrentUserId()

            // 获取任务定义以确认 scopeType
            val task = supabase.from(TABLE_TASKS)
                .select {
                    filter {
                        eq("id", taskId)
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<TaskDto>()
                .toDomain()

            val userTask = UserTask(
                id = UUID.randomUUID().toString(),
                userId = userId,
                taskId = taskId,
                scopeType = task.scopeType,
                saveId = saveId,
                status = TaskStatus.IN_PROGRESS
            )

            supabase.from(TABLE_USER_TASKS)
                .insert(userTask.toDto()) { select() }
                .decodeSingle<UserTaskDto>()
                .toDomain(task = task)
        }

    override suspend fun updateProgress(
        userTaskId: String,
        progressValue: Double
    ): Result<UserTask> = runCatching {
        val userId = requireCurrentUserId()
        supabase.from(TABLE_USER_TASKS)
            .update({
                set("progress_value", progressValue)
                set("last_evaluated_at", Instant.now().toString())
            }) {
                filter {
                    eq("id", userTaskId)
                    eq("user_id", userId)
                }
                select()
            }
            .decodeSingle<UserTaskDto>()
            .toDomain()
    }

    override suspend fun completeTask(userTaskId: String): Result<UserTask> = runCatching {
        val userId = requireCurrentUserId()
        supabase.from(TABLE_USER_TASKS)
            .update({
                set("status", "completed")
                set("progress_value", 1.0)
                set("completed_at", Instant.now().toString())
            }) {
                filter {
                    eq("id", userTaskId)
                    eq("user_id", userId)
                }
                select()
            }
            .decodeSingle<UserTaskDto>()
            .toDomain()
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> = runCatching {
        val userId = requireCurrentUserId()
        supabase.from(TABLE_TASKS)
            .delete {
                filter {
                    eq("id", taskId)
                    eq("user_id", userId)
                }
            }
    }

    private fun requireCurrentUserId(): String =
        supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")

    private companion object {
        const val TABLE_TASKS = "tasks"
        const val TABLE_USER_TASKS = "user_tasks"
    }
}
