package com.tenebralis.dreamos.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tenebralis.dreamos.data.local.db.dao.ContextLogDao
import com.tenebralis.dreamos.data.local.db.dao.ContextSettingsDao
import com.tenebralis.dreamos.data.local.db.dao.ImportedFontDao
import com.tenebralis.dreamos.data.local.db.entity.ContextLogEntity
import com.tenebralis.dreamos.data.local.db.entity.ContextSettingsEntity
import com.tenebralis.dreamos.data.local.db.entity.ImportedFontEntity

@Database(
    entities = [
        ImportedFontEntity::class,
        ContextLogEntity::class,
        ContextSettingsEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class TenebralisDatabase : RoomDatabase() {
    abstract fun importedFontDao(): ImportedFontDao
    abstract fun contextLogDao(): ContextLogDao
    abstract fun contextSettingsDao(): ContextSettingsDao
}
