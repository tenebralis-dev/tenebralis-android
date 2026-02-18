package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.mapper.toDto
import com.tenebralis.dreamos.data.remote.dto.PomodoroSessionDto
import com.tenebralis.dreamos.domain.model.PomodoroSession
import com.tenebralis.dreamos.domain.repository.PomodoroRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class PomodoroRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : PomodoroRepository {

    override suspend fun create(session: PomodoroSession): Result<PomodoroSession> = runCatching {
        val userId = requireCurrentUserId()
        require(session.userId == userId) { "session.userId 与当前会话不一致" }

        supabase.from(TABLE)
            .insert(session.toDto()) {
                select()
            }
            .decodeSingle<PomodoroSessionDto>()
            .toDomain()
    }

    override fun getTodaySessions(): Flow<Result<List<PomodoroSession>>> = flow {
        emit(
            runCatching {
                val userId = requireCurrentUserId()
                val todayStart = LocalDate.now().atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME)

                supabase.from(TABLE)
                    .select {
                        filter {
                            eq("user_id", userId)
                            eq("is_completed", true)
                            gte("started_at", todayStart)
                        }
                    }
                    .decodeList<PomodoroSessionDto>()
                    .map { it.toDomain() }
                    .sortedByDescending { it.startedAt }
            }
        )
    }.catch { emit(Result.failure(it)) }

    override suspend fun getWeekSessions(): Result<List<PomodoroSession>> = runCatching {
        val userId = requireCurrentUserId()
        val today = LocalDate.now()
        val weekFields = WeekFields.of(Locale.getDefault())
        val startOfWeek = today.with(weekFields.dayOfWeek(), 1)
        val weekStart = startOfWeek.atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME)

        supabase.from(TABLE)
            .select {
                filter {
                    eq("user_id", userId)
                    eq("is_completed", true)
                    gte("started_at", weekStart)
                }
            }
            .decodeList<PomodoroSessionDto>()
            .map { it.toDomain() }
            .sortedByDescending { it.startedAt }
    }

    private fun requireCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")
    }

    private companion object {
        const val TABLE = "pomodoro_sessions"
    }
}
