package com.tenebralis.dreamos.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tenebralis.dreamos.data.local.db.entity.ImportedFontEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImportedFontDao {

    @Query("SELECT * FROM imported_fonts ORDER BY imported_at DESC")
    fun getAll(): Flow<List<ImportedFontEntity>>

    @Query("SELECT * FROM imported_fonts WHERE id = :id")
    suspend fun getById(id: String): ImportedFontEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ImportedFontEntity)

    @Query("DELETE FROM imported_fonts WHERE id = :id")
    suspend fun deleteById(id: String)
}
