package com.tenebralis.dreamos.domain.model

/**
 * 梦境会话聚合模型
 *
 * 包含进入梦境所需的全部上下文：世界观、身份、存档、叙事对话和 GM NPC。
 */
data class DreamSession(
    val world: World,
    val identity: UserWorldIdentity,
    val save: WorldSaveState,
    val conversation: Conversation,
    val narratorNpc: Npc
)
