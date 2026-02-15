package com.tenebralis.dreamos.domain.model.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 全局记忆类型
 *
 * 适用表：global_memories.memory_type
 * SQL CHECK: memory_type in ('identity','preference','habit','goal','relationship',
 *            'world_rule','event','schedule','emotion','fact','other')
 */
@Serializable
enum class MemoryType {
    @SerialName("identity")     IDENTITY,
    @SerialName("preference")   PREFERENCE,
    @SerialName("habit")        HABIT,
    @SerialName("goal")         GOAL,
    @SerialName("relationship") RELATIONSHIP,
    @SerialName("world_rule")   WORLD_RULE,
    @SerialName("event")        EVENT,
    @SerialName("schedule")     SCHEDULE,
    @SerialName("emotion")      EMOTION,
    @SerialName("fact")         FACT,
    @SerialName("other")        OTHER
}
