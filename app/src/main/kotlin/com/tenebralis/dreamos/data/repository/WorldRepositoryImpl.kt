package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.mapper.toDto
import com.tenebralis.dreamos.data.remote.dto.WorldDto
import com.tenebralis.dreamos.domain.model.World
import com.tenebralis.dreamos.domain.model.enums.WorldStatus
import com.tenebralis.dreamos.domain.repository.WorldRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class WorldRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : WorldRepository {

    override fun getWorlds(): Flow<Result<List<World>>> = flow {
        emit(
            runCatching {
                val userId = requireCurrentUserId()
                fetchByUser(userId)
                    .map { it.toDomain() }
                    .filter { it.status != WorldStatus.DELETED }
                    .sortedWith(
                        compareByDescending<World> { it.updatedAt.orEmpty() }
                            .thenBy { it.name.lowercase() }
                    )
            }
        )
    }.catch { emit(Result.failure(it)) }

    override suspend fun getById(worldId: String): Result<World> = runCatching {
        val userId = requireCurrentUserId()
        require(worldId.isNotBlank()) { "worldId 不能为空" }
        fetchById(worldId = worldId, userId = userId).toDomain()
    }

    override suspend fun create(world: World): Result<World> = runCatching {
        val userId = requireCurrentUserId()
        validateForWrite(world = world, expectedUserId = userId)

        val createdDto = supabase.from(TABLE_WORLDS)
            .insert(world.toDto()) {
                select()
            }
            .decodeSingle<WorldDto>()

        createdDto.toDomain()
    }

    override suspend fun update(world: World): Result<World> = runCatching {
        val userId = requireCurrentUserId()
        validateForWrite(world = world, expectedUserId = userId)

        val updatedDto = supabase.from(TABLE_WORLDS)
            .update(world.toDto()) {
                filter {
                    eq("id", world.id)
                    eq("user_id", userId)
                }
                select()
            }
            .decodeSingle<WorldDto>()

        updatedDto.toDomain()
    }

    override suspend fun delete(worldId: String): Result<Unit> = runCatching {
        val userId = requireCurrentUserId()
        require(worldId.isNotBlank()) { "worldId 不能为空" }

        val existing = fetchById(worldId = worldId, userId = userId)
        supabase.from(TABLE_WORLDS)
            .update(existing.copy(status = WorldStatus.DELETED)) {
                filter {
                    eq("id", worldId)
                    eq("user_id", userId)
                }
            }
    }

    private suspend fun fetchByUser(userId: String): List<WorldDto> {
        return supabase.from(TABLE_WORLDS)
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<WorldDto>()
    }

    private suspend fun fetchById(worldId: String, userId: String): WorldDto {
        return supabase.from(TABLE_WORLDS)
            .select {
                filter {
                    eq("id", worldId)
                    eq("user_id", userId)
                }
            }
            .decodeSingle<WorldDto>()
    }

    private fun validateForWrite(world: World, expectedUserId: String) {
        require(world.id.isNotBlank()) { "world.id 不能为空" }
        require(world.userId == expectedUserId) { "world.userId 与当前会话不一致" }
        require(world.name.trim().isNotEmpty()) { "world 名称不能为空" }
        require(world.status in WorldStatus.entries) { "world.status 非法" }
    }

    private fun requireCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")
    }

    private companion object {
        const val TABLE_WORLDS = "worlds"
    }
}
