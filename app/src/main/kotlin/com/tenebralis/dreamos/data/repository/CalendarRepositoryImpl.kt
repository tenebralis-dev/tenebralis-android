package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.mapper.toDto
import com.tenebralis.dreamos.data.remote.dto.UserCalendarDto
import com.tenebralis.dreamos.domain.model.UserCalendarEvent
import com.tenebralis.dreamos.domain.model.enums.AiVisibility
import com.tenebralis.dreamos.domain.model.enums.ScopeType
import com.tenebralis.dreamos.domain.repository.CalendarRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class CalendarRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : CalendarRepository {

    override fun getByMonth(yearMonth: String): Flow<Result<List<UserCalendarEvent>>> = flow {
        emit(
            runCatching {
                val userId = requireCurrentUserId()
                val ym = YearMonth.parse(yearMonth)
                val startOfMonth = ym.atDay(1).atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME)
                val endOfMonth = ym.atEndOfMonth().atTime(23, 59, 59).format(DateTimeFormatter.ISO_DATE_TIME)

                supabase.from(TABLE)
                    .select {
                        filter {
                            eq("user_id", userId)
                            gte("start_at", startOfMonth)
                            lte("start_at", endOfMonth)
                        }
                    }
                    .decodeList<UserCalendarDto>()
                    .map { it.toDomain() }
                    .sortedBy { it.startAt }
            }
        )
    }.catch { emit(Result.failure(it)) }

    override suspend fun create(event: UserCalendarEvent): Result<UserCalendarEvent> = runCatching {
        val userId = requireCurrentUserId()
        require(event.userId == userId) { "event.userId 与当前会话不一致" }

        supabase.from(TABLE)
            .insert(event.toDto()) {
                select()
            }
            .decodeSingle<UserCalendarDto>()
            .toDomain()
    }

    override suspend fun update(event: UserCalendarEvent): Result<UserCalendarEvent> = runCatching {
        val userId = requireCurrentUserId()
        require(event.userId == userId) { "event.userId 与当前会话不一致" }

        supabase.from(TABLE)
            .update(event.toDto()) {
                filter {
                    eq("id", event.id)
                    eq("user_id", userId)
                }
                select()
            }
            .decodeSingle<UserCalendarDto>()
            .toDomain()
    }

    override suspend fun delete(eventId: String): Result<Unit> = runCatching {
        val userId = requireCurrentUserId()
        require(eventId.isNotBlank()) { "eventId 不能为空" }

        supabase.from(TABLE)
            .delete {
                filter {
                    eq("id", eventId)
                    eq("user_id", userId)
                }
            }
    }

    override suspend fun getForContext(
        visibleSet: Set<AiVisibility>,
        scopeType: ScopeType?,
        scopeId: String?,
        limit: Int
    ): Result<List<UserCalendarEvent>> = runCatching {
        val userId = requireCurrentUserId()
        val now = LocalDate.now()
        val weekLater = now.plusDays(7)
        val startRange = now.atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME)
        val endRange = weekLater.atTime(23, 59, 59).format(DateTimeFormatter.ISO_DATE_TIME)

        supabase.from(TABLE)
            .select {
                filter {
                    eq("user_id", userId)
                    isIn("ai_visibility", visibleSet.map { it.name.lowercase() })
                    gte("start_at", startRange)
                    lte("start_at", endRange)
                }
            }
            .decodeList<UserCalendarDto>()
            .map { it.toDomain() }
            .let { events ->
                if (scopeType != null) {
                    events.filter { it.scopeType == scopeType && (scopeId == null || it.scopeId == scopeId) }
                } else {
                    events
                }
            }
            .sortedBy { it.startAt }
            .take(limit)
    }

    private fun requireCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")
    }

    private companion object {
        const val TABLE = "user_calendar"
    }
}
