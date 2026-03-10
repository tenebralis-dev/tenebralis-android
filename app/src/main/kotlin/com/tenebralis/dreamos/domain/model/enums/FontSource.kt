package com.tenebralis.dreamos.domain.model.enums

/**
 * 字体来源
 *
 * - SYSTEM：系统默认字体（手机自带）
 * - BUILT_IN：内置字体（APK assets）
 * - REMOTE：R2 可下载字体
 * - LOCAL：用户从手机本地导入
 * - URL：用户通过直链 URL 导入
 */
enum class FontSource {
    SYSTEM,
    BUILT_IN,
    REMOTE,
    LOCAL,
    URL
}
