package com.tenebralis.dreamos.domain.model

/**
 * API 服务类型枚举。
 *
 * 替代原有的自由文本 serviceType，提供预设选项与默认 Base URL。
 * 数据库中仍以小写字符串存储（如 "openai_compat"），通过 [fromSerialName] 解析。
 */
enum class ServiceType(
    val displayName: String,
    val serialName: String,
    val defaultBaseUrl: String?
) {
    OPENAI_COMPAT("OpenAI 兼容 / 中转站", "openai_compat", null),
    OPENAI_OFFICIAL("OpenAI 官方", "openai_official", "https://api.openai.com/v1"),
    ANTHROPIC("Claude", "anthropic", "https://api.anthropic.com/v1"),
    GOOGLE("Gemini", "google", "https://generativelanguage.googleapis.com/v1beta/openai"),
    CUSTOM("自定义", "custom", null);

    companion object {
        fun fromSerialName(name: String): ServiceType =
            entries.find { it.serialName.equals(name, ignoreCase = true) }
                ?: OPENAI_COMPAT
    }
}
