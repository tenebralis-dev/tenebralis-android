package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.mapper.toDto
import com.tenebralis.dreamos.data.remote.dto.AchievementDto
import com.tenebralis.dreamos.data.remote.dto.UserAchievementDto
import com.tenebralis.dreamos.domain.model.Achievement
import com.tenebralis.dreamos.domain.model.UserAchievement
import com.tenebralis.dreamos.domain.model.enums.AchievementStatus
import com.tenebralis.dreamos.domain.repository.AchievementRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class AchievementRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : AchievementRepository {

    override fun getAchievements(worldId: String?): Flow<Result<List<Achievement>>> = flow {
        emit(runCatching {
            val userId = requireCurrentUserId()
            supabase.from(TABLE_ACHIEVEMENTS)
                .select {
                    filter {
                        eq("user_id", userId)
                        if (worldId != null) eq("world_id", worldId)
                    }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<AchievementDto>()
                .map { it.toDomain() }
        })
    }.catch { emit(Result.failure(it)) }

    override fun getUserAchievements(
        status: AchievementStatus?
    ): Flow<Result<List<UserAchievement>>> = flow {
        emit(runCatching {
            val userId = requireCurrentUserId()

            val userAchDtos = supabase.from(TABLE_USER_ACHIEVEMENTS)
                .select {
                    filter {
                        eq("user_id", userId)
                        if (status != null) eq("status", status.name.lowercase())
                    }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<UserAchievementDto>()

            if (userAchDtos.isEmpty()) return@runCatching emptyList()

            // 批量查询关联的 achievement 定义
            val achIds = userAchDtos.map { it.achievementId }.distinct()
            val achDtos = supabase.from(TABLE_ACHIEVEMENTS)
                .select {
                    filter {
                        eq("user_id", userId)
                        isIn("id", achIds)
                    }
                }
                .decodeList<AchievementDto>()
            val achMap = achDtos.associate { it.id to it.toDomain() }

            userAchDtos.map { ua -> ua.toDomain(achievement = achMap[ua.achievementId]) }
        })
    }.catch { emit(Result.failure(it)) }

    override suspend fun createAchievement(achievement: Achievement): Result<Achievement> =
        runCatching {
            val userId = requireCurrentUserId()
            require(achievement.userId == userId) { "userId 不一致" }
            require(achievement.name.trim().isNotEmpty()) { "成就名称不能为空" }

            supabase.from(TABLE_ACHIEVEMENTS)
                .insert(achievement.toDto()) { select() }
                .decodeSingle<AchievementDto>()
                .toDomain()
        }

    override suspend fun unlockAchievement(userAchievementId: String): Result<UserAchievement> =
        runCatching {
            val userId = requireCurrentUserId()
            supabase.from(TABLE_USER_ACHIEVEMENTS)
                .update({
                    set("status", "unlocked")
                    set("progress_value", 1.0)
                    set("unlocked_at", Instant.now().toString())
                }) {
                    filter {
                        eq("id", userAchievementId)
                        eq("user_id", userId)
                    }
                    select()
                }
                .decodeSingle<UserAchievementDto>()
                .toDomain()
        }

    override suspend fun updateProgress(
        userAchievementId: String,
        progressValue: Double
    ): Result<UserAchievement> = runCatching {
        val userId = requireCurrentUserId()
        supabase.from(TABLE_USER_ACHIEVEMENTS)
            .update({
                set("progress_value", progressValue)
                set("last_evaluated_at", Instant.now().toString())
            }) {
                filter {
                    eq("id", userAchievementId)
                    eq("user_id", userId)
                }
                select()
            }
            .decodeSingle<UserAchievementDto>()
            .toDomain()
    }

    private fun requireCurrentUserId(): String =
        supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")

    private companion object {
        const val TABLE_ACHIEVEMENTS = "achievements"
        const val TABLE_USER_ACHIEVEMENTS = "user_achievements"
    }
}
