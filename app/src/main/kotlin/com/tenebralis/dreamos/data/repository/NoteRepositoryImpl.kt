package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.mapper.toDto
import com.tenebralis.dreamos.data.remote.dto.UserNoteDto
import com.tenebralis.dreamos.domain.model.UserNote
import com.tenebralis.dreamos.domain.model.enums.AiVisibility
import com.tenebralis.dreamos.domain.model.enums.ScopeType
import com.tenebralis.dreamos.domain.repository.NoteRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class NoteRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : NoteRepository {

    override fun getAll(): Flow<Result<List<UserNote>>> = flow {
        emit(
            runCatching {
                val userId = requireCurrentUserId()
                supabase.from(TABLE)
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<UserNoteDto>()
                    .map { it.toDomain() }
                    .sortedWith(
                        compareByDescending<UserNote> { it.isPinned }
                            .thenByDescending { it.updatedAt.orEmpty() }
                    )
            }
        )
    }.catch { emit(Result.failure(it)) }

    override suspend fun create(note: UserNote): Result<UserNote> = runCatching {
        val userId = requireCurrentUserId()
        require(note.userId == userId) { "note.userId 与当前会话不一致" }

        supabase.from(TABLE)
            .insert(note.toDto()) {
                select()
            }
            .decodeSingle<UserNoteDto>()
            .toDomain()
    }

    override suspend fun update(note: UserNote): Result<UserNote> = runCatching {
        val userId = requireCurrentUserId()
        require(note.userId == userId) { "note.userId 与当前会话不一致" }

        supabase.from(TABLE)
            .update(note.toDto()) {
                filter {
                    eq("id", note.id)
                    eq("user_id", userId)
                }
                select()
            }
            .decodeSingle<UserNoteDto>()
            .toDomain()
    }

    override suspend fun delete(noteId: String): Result<Unit> = runCatching {
        val userId = requireCurrentUserId()
        require(noteId.isNotBlank()) { "noteId 不能为空" }

        supabase.from(TABLE)
            .delete {
                filter {
                    eq("id", noteId)
                    eq("user_id", userId)
                }
            }
    }

    override suspend fun getForContext(
        visibleSet: Set<AiVisibility>,
        scopeType: ScopeType?,
        scopeId: String?,
        limit: Int
    ): Result<List<UserNote>> = runCatching {
        val userId = requireCurrentUserId()

        supabase.from(TABLE)
            .select {
                filter {
                    eq("user_id", userId)
                    isIn("ai_visibility", visibleSet.map { it.name.lowercase() })
                }
            }
            .decodeList<UserNoteDto>()
            .map { it.toDomain() }
            .let { notes ->
                if (scopeType != null) {
                    notes.filter { it.scopeType == scopeType && (scopeId == null || it.scopeId == scopeId) }
                } else {
                    notes
                }
            }
            .sortedByDescending { it.isPinned }
            .take(limit)
    }

    private fun requireCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")
    }

    private companion object {
        const val TABLE = "user_notes"
    }
}
