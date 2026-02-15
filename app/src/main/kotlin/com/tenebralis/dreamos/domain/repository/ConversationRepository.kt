package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.Conversation
import kotlinx.coroutines.flow.Flow

/**
 * 会话线程仓库接口
 *
 * 对应表：conversations
 */
interface ConversationRepository {

    /** 获取指定存档下的所有会话（按 lastMessageAt 降序） */
    fun getBySave(saveId: String): Flow<Result<List<Conversation>>>

    /**
     * 获取或创建会话线程
     *
     * 按 (userId, saveId, npcId, threadKey) 唯一定位，不存在则创建。
     */
    suspend fun getOrCreate(
        saveId: String,
        npcId: String,
        threadKey: String
    ): Result<Conversation>

    /** 更新会话的 lastMessageAt 和 summary */
    suspend fun updateLastMessage(
        conversationId: String,
        lastMessageAt: String,
        summary: String?
    ): Result<Unit>
}
