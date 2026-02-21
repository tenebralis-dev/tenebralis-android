package com.tenebralis.dreamos.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tenebralis.dreamos.data.local.db.dao.ImportedFontDao
import com.tenebralis.dreamos.data.local.db.entity.ImportedFontEntity

@Database(
    entities = [ImportedFontEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TenebralisDatabase : RoomDatabase() {
    abstract fun importedFontDao(): ImportedFontDao
}
