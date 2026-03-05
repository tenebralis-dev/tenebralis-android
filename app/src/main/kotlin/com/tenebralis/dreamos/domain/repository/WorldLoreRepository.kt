package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.WorldLoreBook
import com.tenebralis.dreamos.domain.model.WorldLoreEntry
import kotlinx.coroutines.flow.Flow

/**
 * 世界书仓库接口
 *
 * 对应表：world_lore_books + world_lore_entries
 * 存储桶：lorebooks
 */
interface WorldLoreRepository {

    /** 获取当前用户的所有世界书 */
    fun getAllBooks(): Flow<Result<List<WorldLoreBook>>>

    /** 获取已启用的世界书（用于激活引擎） */
    suspend fun getEnabledBooks(): Result<List<WorldLoreBook>>

    suspend fun getBookById(id: String): Result<WorldLoreBook>

    suspend fun createBook(book: WorldLoreBook): Result<WorldLoreBook>

    suspend fun updateBook(book: WorldLoreBook): Result<WorldLoreBook>

    /** 删除世界书（同时删除所有条目和 Storage 文件） */
    suspend fun deleteBook(bookId: String): Result<Unit>

    /** 获取某世界书下的所有条目 */
    fun getEntriesByBook(bookId: String): Flow<Result<List<WorldLoreEntry>>>

    /** 获取某世界书下未禁用的条目（用于激活引擎） */
    suspend fun getActiveEntriesByBook(bookId: String): Result<List<WorldLoreEntry>>

    suspend fun getEntryById(id: String): Result<WorldLoreEntry>

    suspend fun createEntry(entry: WorldLoreEntry): Result<WorldLoreEntry>

    /** 批量创建条目（用于导入） */
    suspend fun createEntries(entries: List<WorldLoreEntry>): Result<List<WorldLoreEntry>>

    suspend fun updateEntry(entry: WorldLoreEntry): Result<WorldLoreEntry>

    suspend fun deleteEntry(entryId: String): Result<Unit>

    suspend fun deleteEntriesByBook(bookId: String): Result<Unit>

    /** 上传原始 JSON 到 lorebooks 存储桶 */
    suspend fun uploadOriginalJson(bookId: String, jsonBytes: ByteArray): Result<String>

    /** 从 lorebooks 存储桶下载原始 JSON */
    suspend fun downloadOriginalJson(storagePath: String): Result<ByteArray>
}
