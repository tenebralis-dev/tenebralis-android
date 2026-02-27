package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.UserCalendarEvent
import com.tenebralis.dreamos.domain.model.enums.AiVisibility
import com.tenebralis.dreamos.domain.model.enums.ScopeType
import kotlinx.coroutines.flow.Flow

/**
 * 日历仓库接口
 *
 * 对应表：user_calendar
 */
interface CalendarRepository {

    /** 获取指定月份的日程 */
    fun getByMonth(yearMonth: String): Flow<Result<List<UserCalendarEvent>>>

    /** 创建新日程 */
    suspend fun create(event: UserCalendarEvent): Result<UserCalendarEvent>

    /** 更新日程 */
    suspend fun update(event: UserCalendarEvent): Result<UserCalendarEvent>

    /** 删除日程 */
    suspend fun delete(eventId: String): Result<Unit>

    /**
     * 获取用于 AI 上下文的日程（最近 7 天内）
     */
    suspend fun getForContext(
        visibleSet: Set<AiVisibility>,
        scopeType: ScopeType? = null,
        worldId: String? = null,
        saveId: String? = null,
        limit: Int = 5
    ): Result<List<UserCalendarEvent>>
}
