package com.tenebralis.dreamos.domain.repository

import com.tenebralis.dreamos.domain.model.ContextSettings
import kotlinx.coroutines.flow.Flow

interface ContextSettingsRepository {
    suspend fun get(): ContextSettings
    fun getAsFlow(): Flow<ContextSettings>
    suspend fun update(settings: ContextSettings)
}
