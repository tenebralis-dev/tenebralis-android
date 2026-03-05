package com.tenebralis.dreamos.presentation.screens.chat

sealed interface ChatListEvent {
    data object Refresh : ChatListEvent
    data class SelectNpc(val npcId: String) : ChatListEvent
    data class OpenConversation(val conversationId: String) : ChatListEvent
    data object ConsumeNavigation : ChatListEvent
    data object ClearError : ChatListEvent
    data object ClearInfo : ChatListEvent

    /** 快速创建 NPC */
    data object ShowCreateNpcDialog : ChatListEvent
    data object DismissCreateNpcDialog : ChatListEvent
    data class ConfirmCreateNpc(val name: String, val description: String) : ChatListEvent

    /** 预设选择相关事件 */
    data object DismissPresetPicker : ChatListEvent
    data class ConfirmPresetSelection(val presetId: String?) : ChatListEvent

    /** 新建线程相关事件 */
    data class ShowNewThreadDialog(val npcId: String) : ChatListEvent
    data object DismissNewThreadDialog : ChatListEvent
    data class ConfirmNewThread(val threadName: String) : ChatListEvent
}
