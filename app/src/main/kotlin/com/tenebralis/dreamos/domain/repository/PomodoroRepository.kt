package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.PomodoroSession
import kotlinx.coroutines.flow.Flow

/**
 * 番茄钟仓库接口
 *
 * 对应表：pomodoro_sessions
 */
interface PomodoroRepository {

    /** 创建新的番茄钟记录 */
    suspend fun create(session: PomodoroSession): Result<PomodoroSession>

    /** 获取今日已完成的番茄钟列表 */
    fun getTodaySessions(): Flow<Result<List<PomodoroSession>>>

    /** 获取本周已完成的番茄钟列表 */
    suspend fun getWeekSessions(): Result<List<PomodoroSession>>
}
