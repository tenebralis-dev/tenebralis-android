package com.tenebralis.dreamos.domain.repository

import android.net.Uri
import com.tenebralis.dreamos.domain.model.DownloadProgress
import com.tenebralis.dreamos.domain.model.FontItem
import com.tenebralis.dreamos.domain.model.enums.FontCategory
import kotlinx.coroutines.flow.Flow

/**
 * 字体仓库接口
 *
 * 管理内置字体、R2 可下载字体、用户本地导入字体。
 */
interface FontRepository {

    /** 获取内置字体列表 */
    fun getBuiltInFonts(): List<FontItem>

    /** 从 R2 拉取远端字体索引 */
    suspend fun fetchRemoteFonts(): Result<List<FontItem>>

    /** 获取用户导入的字体列表 */
    fun getImportedFonts(): Flow<List<FontItem>>

    /** 合并全部字体（内置 + 远端 + 导入），按 category 过滤 */
    fun getAllFonts(category: FontCategory): Flow<Result<List<FontItem>>>

    /** 检查字体文件是否已下载到本地 */
    suspend fun isDownloaded(fontId: String, fileName: String): Boolean

    /** 下载远端字体文件 */
    fun downloadFont(item: FontItem): Flow<DownloadProgress>

    /** 从手机本地导入字体文件 */
    suspend fun importLocalFont(
        uri: Uri,
        displayName: String,
        category: FontCategory
    ): Result<FontItem>

    /** 从直链 URL 下载导入字体 */
    fun importUrlFont(
        url: String,
        displayName: String,
        category: FontCategory
    ): Flow<DownloadProgress>

    /** 删除本地字体文件（已下载或已导入） */
    suspend fun deleteFont(fontId: String, fileName: String): Result<Unit>

    /** 获取当前选中字体 ID */
    fun getSelectedFontId(category: FontCategory): Flow<String?>

    /** 设置选中字体 ID */
    suspend fun setSelectedFontId(category: FontCategory, fontId: String)
}
