package com.tenebralis.dreamos.data.repository

import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.mapper.toDto
import com.tenebralis.dreamos.data.remote.dto.WorldLoreBookDto
import com.tenebralis.dreamos.data.remote.dto.WorldLoreEntryDto
import com.tenebralis.dreamos.domain.model.WorldLoreBook
import com.tenebralis.dreamos.domain.model.WorldLoreEntry
import com.tenebralis.dreamos.domain.repository.WorldLoreRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class WorldLoreRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : WorldLoreRepository {

    // ─── Books ────────────────────────────────────────────

    override fun getAllBooks(): Flow<Result<List<WorldLoreBook>>> = flow {
        emit(
            runCatching {
                val userId = requireCurrentUserId()
                supabase.from(TABLE_BOOKS)
                    .select {
                        filter { eq("user_id", userId) }
                    }
                    .decodeList<WorldLoreBookDto>()
                    .sortedBy { it.displayOrder }
                    .map { it.toDomain() }
            }
        )
    }.catch { emit(Result.failure(it)) }

    override suspend fun getEnabledBooks(): Result<List<WorldLoreBook>> = runCatching {
        val userId = requireCurrentUserId()
        supabase.from(TABLE_BOOKS)
            .select {
                filter {
                    eq("user_id", userId)
                    eq("is_enabled", true)
                }
            }
            .decodeList<WorldLoreBookDto>()
            .sortedBy { it.displayOrder }
            .map { it.toDomain() }
    }

    override suspend fun getBookById(id: String): Result<WorldLoreBook> = runCatching {
        val userId = requireCurrentUserId()
        supabase.from(TABLE_BOOKS)
            .select {
                filter {
                    eq("id", id)
                    eq("user_id", userId)
                }
            }
            .decodeSingle<WorldLoreBookDto>()
            .toDomain()
    }

    override suspend fun createBook(book: WorldLoreBook): Result<WorldLoreBook> = runCatching {
        val userId = requireCurrentUserId()
        supabase.from(TABLE_BOOKS)
            .insert(book.toDto(userId)) { select() }
            .decodeSingle<WorldLoreBookDto>()
            .toDomain()
    }

    override suspend fun updateBook(book: WorldLoreBook): Result<WorldLoreBook> = runCatching {
        val userId = requireCurrentUserId()
        supabase.from(TABLE_BOOKS)
            .update(book.toDto(userId)) {
                filter {
                    eq("id", book.id)
                    eq("user_id", userId)
                }
                select()
            }
            .decodeSingle<WorldLoreBookDto>()
            .toDomain()
    }

    override suspend fun deleteBook(bookId: String): Result<Unit> = runCatching {
        val userId = requireCurrentUserId()

        // 先尝试删除 Storage 中的原始文件
        val book = supabase.from(TABLE_BOOKS)
            .select {
                filter {
                    eq("id", bookId)
                    eq("user_id", userId)
                }
            }
            .decodeSingle<WorldLoreBookDto>()

        if (!book.storagePath.isNullOrBlank()) {
            runCatching {
                supabase.storage.from(BUCKET).delete(book.storagePath)
            }
        }

        // 删除数据表记录（条目会 CASCADE 删除）
        supabase.from(TABLE_BOOKS)
            .delete {
                filter {
                    eq("id", bookId)
                    eq("user_id", userId)
                }
            }
    }

    // ─── Entries ──────────────────────────────────────────

    override fun getEntriesByBook(bookId: String): Flow<Result<List<WorldLoreEntry>>> = flow {
        emit(
            runCatching {
                val userId = requireCurrentUserId()
                supabase.from(TABLE_ENTRIES)
                    .select {
                        filter {
                            eq("user_id", userId)
                            eq("book_id", bookId)
                        }
                    }
                    .decodeList<WorldLoreEntryDto>()
                    .sortedBy { it.displayIndex }
                    .map { it.toDomain() }
            }
        )
    }.catch { emit(Result.failure(it)) }

    override suspend fun getActiveEntriesByBook(bookId: String): Result<List<WorldLoreEntry>> =
        runCatching {
            val userId = requireCurrentUserId()
            supabase.from(TABLE_ENTRIES)
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("book_id", bookId)
                        eq("disable", false)
                    }
                }
                .decodeList<WorldLoreEntryDto>()
                .sortedByDescending { it.entryOrder }
                .map { it.toDomain() }
        }

    override suspend fun getEntryById(id: String): Result<WorldLoreEntry> = runCatching {
        val userId = requireCurrentUserId()
        supabase.from(TABLE_ENTRIES)
            .select {
                filter {
                    eq("id", id)
                    eq("user_id", userId)
                }
            }
            .decodeSingle<WorldLoreEntryDto>()
            .toDomain()
    }

    override suspend fun createEntry(entry: WorldLoreEntry): Result<WorldLoreEntry> = runCatching {
        val userId = requireCurrentUserId()
        supabase.from(TABLE_ENTRIES)
            .insert(entry.toDto(userId)) { select() }
            .decodeSingle<WorldLoreEntryDto>()
            .toDomain()
    }

    override suspend fun createEntries(entries: List<WorldLoreEntry>): Result<List<WorldLoreEntry>> =
        runCatching {
            val userId = requireCurrentUserId()
            val dtos = entries.map { it.toDto(userId) }
            supabase.from(TABLE_ENTRIES)
                .insert(dtos) { select() }
                .decodeList<WorldLoreEntryDto>()
                .map { it.toDomain() }
        }

    override suspend fun updateEntry(entry: WorldLoreEntry): Result<WorldLoreEntry> = runCatching {
        val userId = requireCurrentUserId()
        supabase.from(TABLE_ENTRIES)
            .update(entry.toDto(userId)) {
                filter {
                    eq("id", entry.id)
                    eq("user_id", userId)
                }
                select()
            }
            .decodeSingle<WorldLoreEntryDto>()
            .toDomain()
    }

    override suspend fun deleteEntry(entryId: String): Result<Unit> = runCatching {
        val userId = requireCurrentUserId()
        supabase.from(TABLE_ENTRIES)
            .delete {
                filter {
                    eq("id", entryId)
                    eq("user_id", userId)
                }
            }
    }

    override suspend fun deleteEntriesByBook(bookId: String): Result<Unit> = runCatching {
        val userId = requireCurrentUserId()
        supabase.from(TABLE_ENTRIES)
            .delete {
                filter {
                    eq("user_id", userId)
                    eq("book_id", bookId)
                }
            }
    }

    // ─── Storage ──────────────────────────────────────────

    override suspend fun uploadOriginalJson(
        bookId: String,
        jsonBytes: ByteArray
    ): Result<String> = runCatching {
        val userId = requireCurrentUserId()
        val path = "$userId/$bookId/original.json"
        supabase.storage.from(BUCKET).upload(path, jsonBytes) {
            upsert = true
        }
        path
    }

    override suspend fun downloadOriginalJson(storagePath: String): Result<ByteArray> =
        runCatching {
            supabase.storage.from(BUCKET).downloadAuthenticated(storagePath)
        }

    // ─── Helpers ──────────────────────────────────────────

    private fun requireCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("当前未登录")
    }

    private companion object {
        const val TABLE_BOOKS = "world_lore_books"
        const val TABLE_ENTRIES = "world_lore_entries"
        const val BUCKET = "lorebooks"
    }
}
