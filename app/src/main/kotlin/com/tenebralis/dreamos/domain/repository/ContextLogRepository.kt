package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.ContextLog
import kotlinx.coroutines.flow.Flow

interface ContextLogRepository {
    fun getAll(): Flow<List<ContextLog>>
    fun getByConversation(conversationId: String): Flow<List<ContextLog>>
    suspend fun getById(id: Long): ContextLog?
    suspend fun save(log: ContextLog): Long
    suspend fun deleteBefore(before: String): Int
    suspend fun deleteAll()
}
