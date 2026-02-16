package com.tenebralis.dreamos.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.tenebralis.dreamos.domain.repository.ConnectionSecretRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ConnectionSecretRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ConnectionSecretRepository {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun saveSecret(connectionId: String, apiKey: String): Result<Unit> = runCatching {
        require(connectionId.isNotBlank()) { "connectionId 不能为空" }
        require(apiKey.isNotBlank()) { "API Key 不能为空" }
        prefs.edit()
            .putString(secretKey(connectionId), apiKey)
            .apply()
    }

    override suspend fun getSecret(connectionId: String): Result<String?> = runCatching {
        require(connectionId.isNotBlank()) { "connectionId 不能为空" }
        prefs.getString(secretKey(connectionId), null)
    }

    override suspend fun clearSecret(connectionId: String): Result<Unit> = runCatching {
        require(connectionId.isNotBlank()) { "connectionId 不能为空" }
        prefs.edit()
            .remove(secretKey(connectionId))
            .apply()
    }

    private fun secretKey(connectionId: String): String = "$SECRET_PREFIX$connectionId"

    private companion object {
        const val PREFS_FILE_NAME = "connection_secret_store"
        const val SECRET_PREFIX = "connection_secret_"
    }
}
