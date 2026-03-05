package com.tenebralis.dreamos.presentation.screens.chat

import com.tenebralis.dreamos.domain.model.AiPreset
import com.tenebralis.dreamos.domain.model.Conversation
import com.tenebralis.dreamos.domain.model.Npc

data class ChatListUiState(
    val saveId: String? = null,
    val isLoading: Boolean = false,
    val isInitializing: Boolean = false,
    val isCreatingConversation: Boolean = false,
    val isCreatingNpc: Boolean = false,
    val showCreateNpcDialog: Boolean = false,
    val selectedNpcId: String? = null,
    val conversations: List<Conversation> = emptyList(),
    val npcs: List<Npc> = emptyList(),
    val navigateToConversationId: String? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    /** 可用的预设列表 */
    val availablePresets: List<AiPreset> = emptyList(),
    /** 是否正在显示预设选择底部弹窗 */
    val showPresetPicker: Boolean = false,
    /** 等待预设选择的 NPC ID */
    val pendingNpcId: String? = null,
    /** 是否正在显示新建线程命名弹窗 */
    val showNewThreadDialog: Boolean = false,
    /** 正在为哪个 NPC 创建新线程 */
    val newThreadNpcId: String? = null,
    /** 自动生成的默认线程名称 */
    val newThreadDefaultName: String = ""
) {
    val emptyConversationState: Boolean get() = !isLoading && !isInitializing && conversations.isEmpty()
    val emptyNpcState: Boolean get() = !isLoading && !isInitializing && npcs.isEmpty()
}
