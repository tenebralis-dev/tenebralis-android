package com.tenebralis.dreamos.domain.model

/**
 * NPC 好感度关系（领域模型）
 *
 * 对应表：user_npc_relationships
 * status 由客户端根据 affinity 自动计算，不存数据库
 */
data class Relationship(
    val id: String,
    val userId: String,
    val worldId: String,
    val npcId: String,
    val affinity: Int = 0,
    val status: String = "neutral",
    val flagsJson: String = "{}",
    val createdAt: String? = null,
    val updatedAt: String? = null,
    /** 联查字段：关联的 NPC 名称 */
    val npcName: String? = null
) {
    /** 客户端计算好感等级 */
    val tier: AffinityTier get() = AffinityTiers.getTier(affinity)
}
