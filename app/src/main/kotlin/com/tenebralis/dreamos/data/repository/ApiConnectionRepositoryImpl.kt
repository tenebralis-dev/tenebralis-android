package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.mapper.toDto
import com.tenebralis.dreamos.data.remote.dto.ApiConnectionDto
import com.tenebralis.dreamos.domain.model.ApiConnection
import com.tenebralis.dreamos.domain.repository.ApiConnectionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class ApiConnectionRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : ApiConnectionRepository {

    override fun getAll(): Flow<Result<List<ApiConnection>>> = flow {
        emit(
            runCatching {
                val userId = requireCurrentUserId()
                fetchByUser(userId)
                    .map { it.toDomain() }
                    .sortedWith(
                        compareByDescending<ApiConnection> { it.isActive }
                            .thenBy { it.name.lowercase() }
                    )
            }
        )
    }.catch { emit(Result.failure(it)) }

    override suspend fun getActive(): Result<ApiConnection?> = runCatching {
        val userId = requireCurrentUserId()
        fetchByUser(userId)
            .firstOrNull { it.isActive }
            ?.toDomain()
    }

    override suspend fun create(connection: ApiConnection): Result<ApiConnection> = runCatching {
        val userId = requireCurrentUserId()
        require(connection.userId == userId) { "连接用户与当前会话不匹配" }

        if (connection.isActive) {
            deactivateOthers(userId, exceptConnectionId = connection.id)
        }

        val createdDto = supabase.from(TABLE_API_CONNECTIONS)
            .insert(connection.toDto()) {
                select()
            }
            .decodeSingle<ApiConnectionDto>()

        enforceSingleActive(userId)
        createdDto.toDomain()
    }

    override suspend fun update(connection: ApiConnection): Result<ApiConnection> = runCatching {
        val userId = requireCurrentUserId()
        require(connection.userId == userId) { "连接用户与当前会话不匹配" }

        if (connection.isActive) {
            deactivateOthers(userId, exceptConnectionId = connection.id)
        }

        val updatedDto = supabase.from(TABLE_API_CONNECTIONS)
            .update(connection.toDto()) {
                filter {
                    eq("id", connection.id)
                    eq("user_id", userId)
                }
                select()
            }
            .decodeSingle<ApiConnectionDto>()

        enforceSingleActive(userId)
        updatedDto.toDomain()
    }

    override suspend fun delete(connectionId: String): Result<Unit> = runCatching {
        val userId = requireCurrentUserId()

        supabase.from(TABLE_API_CONNECTIONS)
            .delete {
                filter {
                    eq("id", connectionId)
                    eq("user_id", userId)
                }
            }

        enforceSingleActive(userId)
    }

    override suspend fun setActive(connectionId: String): Result<Unit> = runCatching {
        val userId = requireCurrentUserId()
        val allConnections = fetchByUser(userId)
        val target = allConnections.firstOrNull { it.id == connectionId }
            ?: throw IllegalArgumentException("连接不存在或无权限操作")

        deactivateOthers(userId, exceptConnectionId = connectionId)

        if (!target.isActive) {
            supabase.from(TABLE_API_CONNECTIONS)
                .update(target.copy(isActive = true)) {
                    filter {
                        eq("id", connectionId)
                        eq("user_id", userId)
                    }
                }
        }
    }

    private suspend fun fetchByUser(userId: String): List<ApiConnectionDto> {
        return supabase.from(TABLE_API_CONNECTIONS)
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<ApiConnectionDto>()
    }

    private suspend fun deactivateOthers(userId: String, exceptConnectionId: String) {
        val allConnections = fetchByUser(userId)
        allConnections
            .asSequence()
            .filter { it.id != exceptConnectionId && it.isActive }
            .forEach { dto ->
                supabase.from(TABLE_API_CONNECTIONS)
                    .update(dto.copy(isActive = false)) {
                        filter {
                            eq("id", dto.id)
                            eq("user_id", userId)
                        }
                    }
            }
    }

    private suspend fun enforceSingleActive(userId: String) {
        val allConnections = fetchByUser(userId)
        if (allConnections.isEmpty()) return

        val activeConnections = allConnections.filter { it.isActive }
        when {
            activeConnections.isEmpty() -> {
                val first = allConnections.first()
                supabase.from(TABLE_API_CONNECTIONS)
                    .update(first.copy(isActive = true)) {
                        filter {
                            eq("id", first.id)
                            eq("user_id", userId)
                        }
                    }
            }

            activeConnections.size > 1 -> {
                val keep = activeConnections.first()
                activeConnections
                    .filter { it.id != keep.id }
                    .forEach { duplicated ->
                        supabase.from(TABLE_API_CONNECTIONS)
                            .update(duplicated.copy(isActive = false)) {
                                filter {
                                    eq("id", duplicated.id)
                                    eq("user_id", userId)
                                }
                            }
                    }
            }
        }
    }

    private fun requireCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")
    }

    private companion object {
        const val TABLE_API_CONNECTIONS = "api_connections"
    }
}
