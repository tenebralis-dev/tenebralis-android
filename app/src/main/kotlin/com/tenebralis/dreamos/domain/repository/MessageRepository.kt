package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.ConversationMessage
import kotlinx.coroutines.flow.Flow

/**
 * 消息仓库接口
 *
 * 对应表：conversation_messages
 */
interface MessageRepository {

    /** 获取指定会话的消息列表（按 seq 升序） */
    fun getByConversation(conversationId: String): Flow<Result<List<ConversationMessage>>>

    /** 发送（插入）一条消息 */
    suspend fun send(message: ConversationMessage): Result<ConversationMessage>

    /** 获取指定会话的下一个 seq 值 */
    suspend fun getNextSeq(conversationId: String): Result<Int>
}
