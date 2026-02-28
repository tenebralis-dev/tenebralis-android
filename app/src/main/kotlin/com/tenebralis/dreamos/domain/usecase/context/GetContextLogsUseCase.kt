package com.tenebralis.dreamos.domain.usecase.context

import com.tenebralis.dreamos.domain.model.ContextLog
import com.tenebralis.dreamos.domain.repository.ContextLogRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * 获取上下文日志列表。
 */
class GetContextLogsUseCase @Inject constructor(
    private val repository: ContextLogRepository
) {
    /** 获取全部日志（按时间倒序） */
    operator fun invoke(): Flow<List<ContextLog>> = repository.getAll()

    /** 按会话筛选 */
    fun byConversation(conversationId: String): Flow<List<ContextLog>> =
        repository.getByConversation(conversationId)

    /** 获取单条日志详情 */
    suspend fun getById(id: Long): ContextLog? = repository.getById(id)
}
