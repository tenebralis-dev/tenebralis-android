package com.tenebralis.dreamos.domain.usecase.dream

/**
 * 梦境入口解析结果。
 *
 * 内部术语映射：
 * - World = Narrative Template
 * - Identity = Player Role
 * - SaveState = Dream Runtime
 * - Conversation = NPC Interaction Thread
 */
sealed interface DreamEntryDestination {

    data object WorldSelection : DreamEntryDestination

    data class IdentitySelection(
        val worldId: String
    ) : DreamEntryDestination

    data class SaveSelection(
        val worldId: String,
        val identityId: String
    ) : DreamEntryDestination

    data class ConversationSelection(
        val saveId: String
    ) : DreamEntryDestination

    data class Error(
        val message: String
    ) : DreamEntryDestination
}
