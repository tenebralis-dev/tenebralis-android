package com.tenebralis.dreamos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * fonts.json 顶级结构 DTO
 */
@Serializable
data class FontsJsonDto(
    val version: Int,
    @SerialName("baseUrl") val baseUrl: String,
    @SerialName("licenseUrl") val licenseUrl: String? = null,
    val fonts: List<FontItemDto>
)

/**
 * fonts.json 中单个字体条目 DTO
 */
@Serializable
data class FontItemDto(
    val id: String,
    @SerialName("displayName") val displayName: String,
    @SerialName("fileName") val fileName: String,
    @SerialName("fileSize") val fileSize: Long,
    val style: String = "",
    val category: String = "display",
    val tags: List<String> = emptyList(),
    @SerialName("sortOrder") val sortOrder: Int = 0,
    @SerialName("isVariable") val isVariable: Boolean = false,
    val preview: String = ""
)
