package com.tenebralis.dreamos.data.mapper

import com.tenebralis.dreamos.data.local.db.entity.ImportedFontEntity
import com.tenebralis.dreamos.data.remote.dto.FontItemDto
import com.tenebralis.dreamos.domain.model.FontItem
import com.tenebralis.dreamos.domain.model.enums.FontCategory
import com.tenebralis.dreamos.domain.model.enums.FontSource

/**
 * FontItemDto → FontItem（远端字体）
 */
fun FontItemDto.toDomain(isDownloaded: Boolean = false) = FontItem(
    id = id,
    displayName = displayName,
    fileName = fileName,
    fileSize = fileSize,
    style = style,
    category = when (category) {
        "code" -> FontCategory.CODE
        else -> FontCategory.DISPLAY
    },
    tags = tags,
    sortOrder = sortOrder,
    isVariable = isVariable,
    preview = preview,
    source = FontSource.REMOTE,
    isDownloaded = isDownloaded
)

/**
 * ImportedFontEntity → FontItem（本地/URL 导入字体）
 */
fun ImportedFontEntity.toDomain() = FontItem(
    id = id,
    displayName = displayName,
    fileName = fileName,
    fileSize = fileSize,
    style = "",
    category = when (category) {
        "code" -> FontCategory.CODE
        else -> FontCategory.DISPLAY
    },
    tags = emptyList(),
    sortOrder = Int.MAX_VALUE, // 导入字体排在末尾
    isVariable = false,
    preview = if (category == "code") "fn main() { println(\"Hello\"); }" else "春江潮水连海平",
    source = when (source) {
        "URL" -> FontSource.URL
        else -> FontSource.LOCAL
    },
    isDownloaded = true // 导入即下载
)
