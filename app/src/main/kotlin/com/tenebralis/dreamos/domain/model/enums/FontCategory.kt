package com.tenebralis.dreamos.domain.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 字体用途分类
 *
 * - DISPLAY：全局显示字体
 * - CODE：代码专用字体
 */
@Serializable
enum class FontCategory {
    @SerialName("display") DISPLAY,
    @SerialName("code") CODE
}
