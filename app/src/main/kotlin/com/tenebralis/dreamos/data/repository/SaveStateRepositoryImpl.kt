package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.mapper.toDto
import com.tenebralis.dreamos.data.remote.dto.WorldSaveStateDto
import com.tenebralis.dreamos.domain.model.WorldSaveState
import com.tenebralis.dreamos.domain.repository.SaveStateRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class SaveStateRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : SaveStateRepository {

    override fun getByIdentity(identityId: String): Flow<Result<List<WorldSaveState>>> = flow {
        emit(
            runCatching {
                val userId = requireCurrentUserId()
                require(identityId.isNotBlank()) { "identityId 不能为空" }

                fetchByIdentity(userId = userId, identityId = identityId)
                    .map { it.toDomain() }
                    .sortedWith(
                        compareByDescending<WorldSaveState> { it.lastPlayedAt.orEmpty() }
                            .thenByDescending { it.updatedAt.orEmpty() }
                            .thenBy { it.slot }
                    )
            }
        )
    }.catch { emit(Result.failure(it)) }

    override suspend fun getById(saveId: String): Result<WorldSaveState> = runCatching {
        val userId = requireCurrentUserId()
        require(saveId.isNotBlank()) { "saveId 不能为空" }
        supabase.from(TABLE_SAVE_STATES)
            .select {
                filter {
                    eq("id", saveId)
                    eq("user_id", userId)
                }
            }
            .decodeSingle<WorldSaveStateDto>()
            .toDomain()
    }

    override suspend fun create(saveState: WorldSaveState): Result<WorldSaveState> = runCatching {
        val userId = requireCurrentUserId()
        validateForWrite(saveState = saveState, expectedUserId = userId)

        val created = supabase.from(TABLE_SAVE_STATES)
            .insert(saveState.toDto()) {
                select()
            }
            .decodeSingle<WorldSaveStateDto>()

        created.toDomain()
    }

    override suspend fun update(saveState: WorldSaveState): Result<WorldSaveState> = runCatching {
        val userId = requireCurrentUserId()
        validateForWrite(saveState = saveState, expectedUserId = userId)

        val updated = supabase.from(TABLE_SAVE_STATES)
            .update(saveState.toDto()) {
                filter {
                    eq("id", saveState.id)
                    eq("user_id", userId)
                }
                select()
            }
            .decodeSingle<WorldSaveStateDto>()

        updated.toDomain()
    }

    private suspend fun fetchByIdentity(userId: String, identityId: String): List<WorldSaveStateDto> {
        return supabase.from(TABLE_SAVE_STATES)
            .select {
                filter {
                    eq("user_id", userId)
                    eq("identity_id", identityId)
                }
            }
            .decodeList<WorldSaveStateDto>()
    }

    private fun validateForWrite(saveState: WorldSaveState, expectedUserId: String) {
        require(saveState.id.isNotBlank()) { "saveState.id 不能为空" }
        require(saveState.userId == expectedUserId) { "saveState.userId 与当前会话不一致" }
        require(saveState.worldId.isNotBlank()) { "saveState.worldId 不能为空" }
        require(saveState.identityId.isNotBlank()) { "saveState.identityId 不能为空" }
        require(saveState.slot > 0) { "saveState.slot 必须大于 0" }
    }

    private fun requireCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")
    }

    private companion object {
        const val TABLE_SAVE_STATES = "world_save_states"
    }
}
