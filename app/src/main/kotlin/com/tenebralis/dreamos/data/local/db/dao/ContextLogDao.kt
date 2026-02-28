package com.tenebralis.dreamos.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tenebralis.dreamos.data.local.db.entity.ContextLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContextLogDao {

    @Query("SELECT * FROM context_logs ORDER BY created_at DESC")
    fun getAll(): Flow<List<ContextLogEntity>>

    @Query("SELECT * FROM context_logs WHERE conversation_id = :conversationId ORDER BY created_at DESC")
    fun getByConversation(conversationId: String): Flow<List<ContextLogEntity>>

    @Query("SELECT * FROM context_logs WHERE id = :id")
    suspend fun getById(id: Long): ContextLogEntity?

    @Insert
    suspend fun insert(log: ContextLogEntity): Long

    @Query("DELETE FROM context_logs WHERE created_at < :before")
    suspend fun deleteBefore(before: String): Int

    @Query("DELETE FROM context_logs")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM context_logs")
    suspend fun count(): Int
}
