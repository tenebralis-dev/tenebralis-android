package com.tenebralis.dreamos.domain.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 全局记忆来源类型
 *
 * 适用表：global_memories.source_type
 * SQL CHECK: source_type in ('manual','conversation','note','calendar',
 *            'ledger','media','pomodoro','system','import')
 */
@Serializable
enum class MemorySourceType {
    @SerialName("manual")       MANUAL,
    @SerialName("conversation") CONVERSATION,
    @SerialName("note")         NOTE,
    @SerialName("calendar")     CALENDAR,
    @SerialName("ledger")       LEDGER,
    @SerialName("media")        MEDIA,
    @SerialName("pomodoro")     POMODORO,
    @SerialName("system")       SYSTEM,
    @SerialName("import")       IMPORT
}
