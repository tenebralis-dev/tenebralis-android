package com.tenebralis.dreamos.presentation.screens.dream

import com.tenebralis.dreamos.domain.model.ConversationMessage
import com.tenebralis.dreamos.domain.model.DreamSession

/**
 * 梦境 UI 状态
 */
data class DreamUiState(
    /** 梦境会话上下文（加载完毕后非 null） */
    val session: DreamSession? = null,

    /** 是否正在初始化梦境 */
    val isInitializing: Boolean = true,

    /** 叙事消息列表 */
    val messages: List<ConversationMessage> = emptyList(),

    /** 用户输入 */
    val inputText: String = "",

    /** 是否正在发送 */
    val isSending: Boolean = false,

    /** AI 是否正在回复 */
    val isAiResponding: Boolean = false,

    /** AI 错误消息 */
    val aiErrorMessage: String? = null,

    /** 通用错误消息 */
    val errorMessage: String? = null,

    /** 信息提示 */
    val infoMessage: String? = null,

    /** 上下文面板是否展开 */
    val isContextExpanded: Boolean = true,

    /** 上次发送失败的内容 */
    val failedContent: String? = null
) {
    val isReady: Boolean get() = session != null && !isInitializing
    val emptyState: Boolean get() = isReady && messages.isEmpty()
}
