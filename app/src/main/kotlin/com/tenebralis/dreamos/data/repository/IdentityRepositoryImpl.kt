package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.mapper.toDto
import com.tenebralis.dreamos.data.remote.dto.UserWorldIdentityDto
import com.tenebralis.dreamos.domain.model.UserWorldIdentity
import com.tenebralis.dreamos.domain.repository.IdentityRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class IdentityRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : IdentityRepository {

    override fun getByWorld(worldId: String): Flow<Result<List<UserWorldIdentity>>> = flow {
        emit(
            runCatching {
                val userId = requireCurrentUserId()
                require(worldId.isNotBlank()) { "worldId 不能为空" }
                fetchByWorld(userId = userId, worldId = worldId)
                    .map { it.toDomain() }
                    .sortedWith(
                        compareByDescending<UserWorldIdentity> { it.isActive }
                            .thenBy { it.identityName.lowercase() }
                    )
            }
        )
    }.catch { emit(Result.failure(it)) }

    override suspend fun create(identity: UserWorldIdentity): Result<UserWorldIdentity> = runCatching {
        val userId = requireCurrentUserId()
        validateForWrite(identity = identity, expectedUserId = userId)

        if (identity.isActive) {
            deactivateOthers(userId, worldId = identity.worldId, exceptIdentityId = identity.id)
        }

        val created = supabase.from(TABLE_IDENTITIES)
            .insert(identity.toDto()) {
                select()
            }
            .decodeSingle<UserWorldIdentityDto>()

        if (created.isActive) {
            deactivateOthers(
                userId = userId,
                worldId = created.worldId,
                exceptIdentityId = created.id
            )
        } else {
            ensureAtLeastOneActive(
                userId = userId,
                worldId = created.worldId,
                preferredIdentityId = created.id
            )
        }

        created.toDomain()
    }

    override suspend fun update(identity: UserWorldIdentity): Result<UserWorldIdentity> = runCatching {
        val userId = requireCurrentUserId()
        validateForWrite(identity = identity, expectedUserId = userId)

        if (identity.isActive) {
            deactivateOthers(userId, worldId = identity.worldId, exceptIdentityId = identity.id)
        }

        val updated = supabase.from(TABLE_IDENTITIES)
            .update(identity.toDto()) {
                filter {
                    eq("id", identity.id)
                    eq("user_id", userId)
                }
                select()
            }
            .decodeSingle<UserWorldIdentityDto>()

        if (updated.isActive) {
            deactivateOthers(
                userId = userId,
                worldId = updated.worldId,
                exceptIdentityId = updated.id
            )
        } else {
            ensureAtLeastOneActive(
                userId = userId,
                worldId = updated.worldId,
                preferredIdentityId = updated.id
            )
        }

        updated.toDomain()
    }

    override suspend fun setActive(worldId: String, identityId: String): Result<Unit> = runCatching {
        val userId = requireCurrentUserId()
        require(worldId.isNotBlank()) { "worldId 不能为空" }
        require(identityId.isNotBlank()) { "identityId 不能为空" }

        val allIdentities = fetchByWorld(userId = userId, worldId = worldId)
        val target = allIdentities.firstOrNull { it.id == identityId }
            ?: throw IllegalArgumentException("身份不存在或无权限访问")

        deactivateOthers(userId = userId, worldId = worldId, exceptIdentityId = identityId)

        if (!target.isActive) {
            supabase.from(TABLE_IDENTITIES)
                .update(target.copy(isActive = true)) {
                    filter {
                        eq("id", identityId)
                        eq("user_id", userId)
                    }
                }
        }
    }

    private suspend fun fetchByWorld(userId: String, worldId: String): List<UserWorldIdentityDto> {
        return supabase.from(TABLE_IDENTITIES)
            .select {
                filter {
                    eq("user_id", userId)
                    eq("world_id", worldId)
                }
            }
            .decodeList<UserWorldIdentityDto>()
    }

    private suspend fun deactivateOthers(
        userId: String,
        worldId: String,
        exceptIdentityId: String
    ) {
        fetchByWorld(userId = userId, worldId = worldId)
            .asSequence()
            .filter { it.id != exceptIdentityId && it.isActive }
            .forEach { identity ->
                supabase.from(TABLE_IDENTITIES)
                    .update(identity.copy(isActive = false)) {
                        filter {
                            eq("id", identity.id)
                            eq("user_id", userId)
                        }
                    }
            }
    }

    private suspend fun ensureAtLeastOneActive(
        userId: String,
        worldId: String,
        preferredIdentityId: String?
    ) {
        val allIdentities = fetchByWorld(userId = userId, worldId = worldId)
        if (allIdentities.isEmpty() || allIdentities.any { it.isActive }) return

        val target = allIdentities.firstOrNull { it.id == preferredIdentityId } ?: allIdentities.first()
        supabase.from(TABLE_IDENTITIES)
            .update(target.copy(isActive = true)) {
                filter {
                    eq("id", target.id)
                    eq("user_id", userId)
                }
            }
    }

    private fun validateForWrite(identity: UserWorldIdentity, expectedUserId: String) {
        require(identity.id.isNotBlank()) { "identity.id 不能为空" }
        require(identity.userId == expectedUserId) { "identity.userId 与当前会话不一致" }
        require(identity.worldId.isNotBlank()) { "identity.worldId 不能为空" }
        require(identity.identityName.trim().isNotEmpty()) { "identity 名称不能为空" }
    }

    private fun requireCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")
    }

    private companion object {
        const val TABLE_IDENTITIES = "user_world_identities"
    }
}
