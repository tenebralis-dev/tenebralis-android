package com.tenebralis.dreamos.domain.model

/**
 * 好感度分级定义
 *
 * 7 级系统，范围 -100 ~ 100，由客户端根据 affinity 值自动计算。
 */
data class AffinityTier(
    val minAffinity: Int,
    val maxAffinity: Int,
    val key: String,
    val displayName: String,
    val colorHex: String
)

object AffinityTiers {
    val DEFAULT = listOf(
        AffinityTier(-100, -51, "hostile",   "敌对", "#EF4444"),
        AffinityTier(-50,  -11, "dislike",   "厌恶", "#F97316"),
        AffinityTier(-10,    9, "neutral",   "中立", "#9CA3AF"),
        AffinityTier(  10,  29, "friendly",  "友好", "#22C55E"),
        AffinityTier(  30,  59, "close",     "亲密", "#3B82F6"),
        AffinityTier(  60,  89, "devoted",   "忠诚", "#A855F7"),
        AffinityTier(  90, 100, "soulbound", "灵魂绊", "#EAB308")
    )

    fun getTier(affinity: Int): AffinityTier =
        DEFAULT.first { affinity in it.minAffinity..it.maxAffinity }
}
