package com.tenebralis.dreamos.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.tenebralis.dreamos.data.local.db.entity.ContextSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContextSettingsDao {

    @Query("SELECT * FROM context_settings WHERE id = 1")
    suspend fun get(): ContextSettingsEntity?

    @Query("SELECT * FROM context_settings WHERE id = 1")
    fun getAsFlow(): Flow<ContextSettingsEntity?>

    @Upsert
    suspend fun upsert(entity: ContextSettingsEntity)
}
