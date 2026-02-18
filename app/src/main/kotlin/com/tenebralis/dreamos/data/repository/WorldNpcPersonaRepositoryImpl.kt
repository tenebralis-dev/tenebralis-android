package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.mapper.toDto
import com.tenebralis.dreamos.data.remote.dto.WorldNpcPersonaDto
import com.tenebralis.dreamos.domain.model.WorldNpcPersona
import com.tenebralis.dreamos.domain.repository.WorldNpcPersonaRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class WorldNpcPersonaRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : WorldNpcPersonaRepository {

    override fun getByWorld(worldId: String): Flow<Result<List<WorldNpcPersona>>> =
        flow {
            emit(runCatching {
                val userId = requireCurrentUserId()
                supabase.from(TABLE)
                    .select {
                        filter {
                            eq("user_id", userId)
                            eq("world_id", worldId)
                        }
                    }
                    .decodeList<WorldNpcPersonaDto>()
                    .map { it.toDomain() }
            })
        }.catch { emit(Result.failure(it)) }

    override suspend fun getById(personaId: String): Result<WorldNpcPersona> = runCatching {
        val userId = requireCurrentUserId()
        supabase.from(TABLE)
            .select {
                filter {
                    eq("id", personaId)
                    eq("user_id", userId)
                }
            }
            .decodeSingle<WorldNpcPersonaDto>()
            .toDomain()
    }

    override suspend fun create(
        worldId: String,
        npcId: String,
        displayName: String,
        personaJson: String,
        avatarUrl: String?,
        promptText: String?
    ): Result<WorldNpcPersona> = runCatching {
        val userId = requireCurrentUserId()
        val persona = WorldNpcPersona(
            id = UUID.randomUUID().toString(),
            userId = userId,
            worldId = worldId,
            npcId = npcId,
            personaJson = personaJson,
            displayName = displayName,
            avatarUrl = avatarUrl,
            promptText = promptText
        )
        supabase.from(TABLE)
            .insert(persona.toDto()) { select() }
            .decodeSingle<WorldNpcPersonaDto>()
            .toDomain()
    }

    private fun requireCurrentUserId(): String =
        supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")

    private companion object {
        const val TABLE = "world_npc_personas"
    }
}
