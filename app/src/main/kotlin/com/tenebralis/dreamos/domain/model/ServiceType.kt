package com.tenebralis.dreamos.domain.model

/**
 * API 服务类型枚举。
 *
 * 当前仅支持 OpenAI 兼容协议；CUSTOM 保留供特殊场景使用。
 * 数据库中仍以小写字符串存储（如 "openai_compat"），通过 [fromSerialName] 解析。
 * 旧值（openai_official / anthropic / google）会自动回退为 OPENAI_COMPAT。
 */
enum class ServiceType(
    val displayName: String,
    val serialName: String,
    val defaultBaseUrl: String?
) {
    OPENAI_COMPAT("OpenAI 兼容", "openai_compat", null),
    CUSTOM("自定义", "custom", null);

    companion object {
        fun fromSerialName(name: String): ServiceType =
            entries.find { it.serialName.equals(name, ignoreCase = true) }
                ?: OPENAI_COMPAT
    }
}
