@file:Suppress("DEPRECATION")

package com.tenebralis.dreamos.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.tenebralis.dreamos.domain.repository.ConnectionSecretRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.GeneralSecurityException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class ConnectionSecretRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ConnectionSecretRepository {

    private val mutex = Mutex()

    @Volatile
    private var cachedPrefs: SharedPreferences? = null

    /**
     * 获取 EncryptedSharedPreferences 实例。
     *
     * 如果底层 keyset 已损坏（重装残留 / KeyStore 变化等），
     * 会自动清除损坏文件并重建，所有已保存的密钥将丢失。
     */
    private suspend fun getOrCreatePrefs(): SharedPreferences {
        cachedPrefs?.let { return it }
        return mutex.withLock {
            cachedPrefs?.let { return it }
            withContext(Dispatchers.IO) {
                try {
                    createEncryptedPrefs().also { cachedPrefs = it }
                } catch (e: GeneralSecurityException) {
                    Log.e(TAG, "EncryptedSharedPreferences 初始化失败，尝试重建", e)
                    deletePrefsFiles()
                    createEncryptedPrefs().also { cachedPrefs = it }
                } catch (e: Exception) {
                    Log.e(TAG, "EncryptedSharedPreferences 意外异常，尝试重建", e)
                    deletePrefsFiles()
                    createEncryptedPrefs().also { cachedPrefs = it }
                }
            }
        }
    }

    private fun createEncryptedPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * 删除损坏的 SharedPreferences 文件。
     *
     * Tink keyset 作为 key-value 存储在同一个 prefs 文件内，
     * 删除该文件即可清除所有 keyset 和密钥数据。
     */
    private fun deletePrefsFiles() {
        try {
            val deleted = context.deleteSharedPreferences(PREFS_FILE_NAME)
            Log.w(TAG, "删除损坏的 $PREFS_FILE_NAME: $deleted")
        } catch (e: Exception) {
            Log.e(TAG, "清理损坏的 Prefs 文件失败", e)
        }
    }

    override suspend fun saveSecret(connectionId: String, apiKey: String): Result<Unit> = runCatching {
        require(connectionId.isNotBlank()) { "connectionId 不能为空" }
        require(apiKey.isNotBlank()) { "API Key 不能为空" }
        withContext(Dispatchers.IO) {
            getOrCreatePrefs().edit()
                .putString(secretKey(connectionId), apiKey)
                .apply()
        }
    }

    override suspend fun getSecret(connectionId: String): Result<String?> = runCatching {
        require(connectionId.isNotBlank()) { "connectionId 不能为空" }
        withContext(Dispatchers.IO) {
            getOrCreatePrefs().getString(secretKey(connectionId), null)
        }
    }

    override suspend fun clearSecret(connectionId: String): Result<Unit> = runCatching {
        require(connectionId.isNotBlank()) { "connectionId 不能为空" }
        withContext(Dispatchers.IO) {
            getOrCreatePrefs().edit()
                .remove(secretKey(connectionId))
                .apply()
        }
    }

    private fun secretKey(connectionId: String): String = "$SECRET_PREFIX$connectionId"

    private companion object {
        const val TAG = "ConnectionSecret"
        const val PREFS_FILE_NAME = "connection_secret_store"
        const val SECRET_PREFIX = "connection_secret_"
    }
}
