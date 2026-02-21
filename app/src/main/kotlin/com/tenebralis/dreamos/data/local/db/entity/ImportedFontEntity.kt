package com.tenebralis.dreamos.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户导入字体的持久化实体
 *
 * 存储通过「从手机选择文件」或「输入字体直链 URL」方式导入的字体元数据。
 */
@Entity(tableName = "imported_fonts")
data class ImportedFontEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "display_name")
    val displayName: String,

    @ColumnInfo(name = "file_name")
    val fileName: String,

    @ColumnInfo(name = "file_size")
    val fileSize: Long,

    /** LOCAL 或 URL */
    val source: String,

    /** 仅 source = URL 时有值 */
    @ColumnInfo(name = "source_url")
    val sourceUrl: String? = null,

    /** display 或 code */
    val category: String = "display",

    @ColumnInfo(name = "imported_at")
    val importedAt: String? = null
)
