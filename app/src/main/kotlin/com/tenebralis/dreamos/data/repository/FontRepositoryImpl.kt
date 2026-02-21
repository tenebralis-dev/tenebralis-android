package com.tenebralis.dreamos.data.repository

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tenebralis.dreamos.data.local.db.dao.ImportedFontDao
import com.tenebralis.dreamos.data.local.db.entity.ImportedFontEntity
import com.tenebralis.dreamos.data.mapper.toDomain
import com.tenebralis.dreamos.data.remote.dto.FontsJsonDto
import com.tenebralis.dreamos.domain.model.DownloadProgress
import com.tenebralis.dreamos.domain.model.FontItem
import com.tenebralis.dreamos.domain.model.enums.FontCategory
import com.tenebralis.dreamos.domain.model.enums.FontSource
import com.tenebralis.dreamos.domain.repository.FontRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

private val Context.fontDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "font_preferences"
)

class FontRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val importedFontDao: ImportedFontDao,
    @param:ApplicationContext private val context: Context
) : FontRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val fontsDir: File
        get() = File(context.filesDir, "fonts").also { it.mkdirs() }

    // ─── 内置字体 ────────────────────────────────────────────

    override fun getBuiltInFonts(): List<FontItem> = listOf(
        FontItem(
            id = BUILT_IN_FONT_ID,
            displayName = "Noto Sans SC",
            fileName = BUILT_IN_FILE_NAME,
            fileSize = 0,
            style = "无衬线黑体",
            category = FontCategory.DISPLAY,
            tags = listOf("中文", "无衬线"),
            sortOrder = -1,
            isVariable = false,
            preview = "春江潮水连海平",
            source = FontSource.BUILT_IN,
            isDownloaded = true
        )
    )

    // ─── 远端字体 ────────────────────────────────────────────

    override suspend fun fetchRemoteFonts(): Result<List<FontItem>> = runCatching {
        withContext(Dispatchers.IO) {
            val fontsJsonDto = fetchFontsJson()
            fontsJsonDto.fonts.map { dto ->
                val downloaded = File(fontsDir, dto.fileName).exists()
                dto.toDomain(isDownloaded = downloaded)
            }
        }
    }

    // ─── 导入字体 ────────────────────────────────────────────

    override fun getImportedFonts(): Flow<List<FontItem>> =
        importedFontDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }

    // ─── 全部字体 ────────────────────────────────────────────

    override fun getAllFonts(category: FontCategory): Flow<Result<List<FontItem>>> {
        val remoteFontsFlow = flow {
            emit(fetchRemoteFonts())
        }

        return combine(remoteFontsFlow, importedFontDao.getAll()) { remoteResult, importedEntities ->
            remoteResult.map { remoteFonts ->
                val builtIn = getBuiltInFonts()
                val imported = importedEntities.map { it.toDomain() }
                (builtIn + remoteFonts + imported)
                    .filter { it.category == category }
                    .sortedWith(
                        compareBy<FontItem> { it.source.ordinal }
                            .thenBy { it.sortOrder }
                    )
            }
        }.catch { emit(Result.failure(it)) }
    }

    // ─── 下载 ────────────────────────────────────────────────

    override suspend fun isDownloaded(fontId: String, fileName: String): Boolean =
        withContext(Dispatchers.IO) { File(fontsDir, fileName).exists() }

    override fun downloadFont(item: FontItem): Flow<DownloadProgress> = flow {
        val baseUrl = getCachedBaseUrl()
        val url = "$baseUrl/${item.fileName}"
        val targetFile = File(fontsDir, item.fileName)

        httpClient.prepareGet(url).execute { response ->
            val channel = response.bodyAsChannel()
            val totalBytes = response.contentLength() ?: item.fileSize
            var downloaded = 0L

            targetFile.outputStream().use { output ->
                val buffer = ByteArray(8192)
                while (!channel.isClosedForRead) {
                    val bytesRead = channel.readAvailable(buffer)
                    if (bytesRead <= 0) break
                    output.write(buffer, 0, bytesRead)
                    downloaded += bytesRead
                    emit(DownloadProgress(item.id, downloaded, totalBytes))
                }
            }
            emit(DownloadProgress(item.id, downloaded, totalBytes, isComplete = true))
        }
    }.flowOn(Dispatchers.IO)

    // ─── 本地导入 ─────────────────────────────────────────────

    override suspend fun importLocalFont(
        uri: Uri,
        displayName: String,
        category: FontCategory
    ): Result<FontItem> = runCatching {
        withContext(Dispatchers.IO) {
            val id = "local-${UUID.randomUUID()}"
            val originalName = uri.lastPathSegment?.substringAfterLast('/') ?: "$id.ttf"
            val safeFileName = "${id}_$originalName"
            val targetFile = File(fontsDir, safeFileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: throw IllegalStateException("无法读取文件")

            val entity = ImportedFontEntity(
                id = id,
                displayName = displayName,
                fileName = safeFileName,
                fileSize = targetFile.length(),
                source = "LOCAL",
                category = if (category == FontCategory.CODE) "code" else "display",
                importedAt = Instant.now().toString()
            )
            importedFontDao.insert(entity)
            entity.toDomain()
        }
    }

    // ─── URL 导入 ─────────────────────────────────────────────

    override fun importUrlFont(
        url: String,
        displayName: String,
        category: FontCategory
    ): Flow<DownloadProgress> = flow {
        val id = "url-${UUID.randomUUID()}"
        val urlFileName = url.substringAfterLast('/').substringBefore('?').ifBlank { "$id.ttf" }
        val safeFileName = "${id}_$urlFileName"
        val targetFile = File(fontsDir, safeFileName)

        httpClient.prepareGet(url).execute { response ->
            val channel = response.bodyAsChannel()
            var downloaded = 0L

            targetFile.outputStream().use { output ->
                val buffer = ByteArray(8192)
                while (!channel.isClosedForRead) {
                    val bytesRead = channel.readAvailable(buffer)
                    if (bytesRead <= 0) break
                    output.write(buffer, 0, bytesRead)
                    downloaded += bytesRead
                    emit(DownloadProgress(id, downloaded, -1))
                }
            }

            val entity = ImportedFontEntity(
                id = id,
                displayName = displayName,
                fileName = safeFileName,
                fileSize = targetFile.length(),
                source = "URL",
                sourceUrl = url,
                category = if (category == FontCategory.CODE) "code" else "display",
                importedAt = Instant.now().toString()
            )
            importedFontDao.insert(entity)

            emit(DownloadProgress(id, targetFile.length(), targetFile.length(), isComplete = true))
        }
    }.flowOn(Dispatchers.IO)

    // ─── 删除 ────────────────────────────────────────────────

    override suspend fun deleteFont(fontId: String, fileName: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            File(fontsDir, fileName).delete()
            importedFontDao.deleteById(fontId)
        }
    }

    // ─── 选中持久化 ──────────────────────────────────────────

    override fun getSelectedFontId(category: FontCategory): Flow<String?> =
        context.fontDataStore.data.map { prefs ->
            prefs[keyForCategory(category)]
        }

    override suspend fun setSelectedFontId(category: FontCategory, fontId: String) {
        context.fontDataStore.edit { prefs ->
            prefs[keyForCategory(category)] = fontId
        }
    }

    // ─── 内部 ────────────────────────────────────────────────

    private fun keyForCategory(category: FontCategory): Preferences.Key<String> =
        when (category) {
            FontCategory.DISPLAY -> KEY_DISPLAY_FONT_ID
            FontCategory.CODE -> KEY_CODE_FONT_ID
        }

    private suspend fun fetchFontsJson(): FontsJsonDto {
        val cacheFile = File(fontsDir, "fonts.json")
        val cacheValid = cacheFile.exists() &&
                (System.currentTimeMillis() - cacheFile.lastModified()) < CACHE_TTL_MS

        return if (cacheValid) {
            json.decodeFromString(cacheFile.readText())
        } else {
            try {
                val response = httpClient.prepareGet(FONTS_JSON_URL).execute { it.bodyAsText() }
                cacheFile.writeText(response)
                json.decodeFromString(response)
            } catch (e: Exception) {
                if (cacheFile.exists()) {
                    json.decodeFromString(cacheFile.readText())
                } else {
                    throw e
                }
            }
        }
    }

    private suspend fun getCachedBaseUrl(): String {
        return try {
            fetchFontsJson().baseUrl
        } catch (_: Exception) {
            DEFAULT_BASE_URL
        }
    }

    companion object {
        const val BUILT_IN_FONT_ID = "noto-sans-sc"
        const val BUILT_IN_FILE_NAME = "NotoSansSC-Regular.ttf"
        const val FONTS_JSON_URL = "https://fonts.tenebralis-dream.com/fonts.json"
        const val DEFAULT_BASE_URL = "https://fonts.tenebralis-dream.com"
        const val CACHE_TTL_MS = 24 * 60 * 60 * 1000L

        private val KEY_DISPLAY_FONT_ID = stringPreferencesKey("display_font_id")
        private val KEY_CODE_FONT_ID = stringPreferencesKey("code_font_id")
    }
}
