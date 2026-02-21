package com.tenebralis.dreamos.domain.model

import com.tenebralis.dreamos.domain.model.enums.FontCategory
import com.tenebralis.dreamos.domain.model.enums.FontSource

/**
 * 字体条目（领域模型）
 *
 * 统一表示内置、远端、本地导入和 URL 导入四种来源的字体。
 */
data class FontItem(
    val id: String,
    val displayName: String,
    val fileName: String,
    val fileSize: Long,
    val style: String = "",
    val category: FontCategory = FontCategory.DISPLAY,
    val tags: List<String> = emptyList(),
    val sortOrder: Int = 0,
    val isVariable: Boolean = false,
    val preview: String = "春江潮水连海平",
    val source: FontSource = FontSource.REMOTE,
    val isDownloaded: Boolean = false
)

/**
 * 下载进度
 */
data class DownloadProgress(
    val fontId: String,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val isComplete: Boolean = false
) {
    val fraction: Float
        get() = if (totalBytes > 0) bytesDownloaded.toFloat() / totalBytes else 0f
}
