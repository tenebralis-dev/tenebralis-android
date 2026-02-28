package com.tenebralis.dreamos.di

import android.app.Application
import androidx.room.Room
import com.tenebralis.dreamos.data.local.db.TenebralisDatabase
import com.tenebralis.dreamos.data.local.db.dao.ContextLogDao
import com.tenebralis.dreamos.data.local.db.dao.ContextSettingsDao
import com.tenebralis.dreamos.data.local.db.dao.ImportedFontDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(application: Application): TenebralisDatabase =
        Room.databaseBuilder(
            application,
            TenebralisDatabase::class.java,
            "tenebralis_db"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideImportedFontDao(db: TenebralisDatabase): ImportedFontDao =
        db.importedFontDao()

    @Provides
    fun provideContextLogDao(db: TenebralisDatabase): ContextLogDao =
        db.contextLogDao()

    @Provides
    fun provideContextSettingsDao(db: TenebralisDatabase): ContextSettingsDao =
        db.contextSettingsDao()
}
