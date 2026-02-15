package com.tenebralis.dreamos.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tenebralis.dreamos.domain.model.RememberedCredential
import com.tenebralis.dreamos.domain.repository.RememberedCredentialRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.authCredentialDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auth_credential_store"
)

class RememberedCredentialRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : RememberedCredentialRepository {

    override fun observeRememberedCredential(): Flow<RememberedCredential?> =
        context.authCredentialDataStore.data
            .catch { throwable ->
                if (throwable is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw throwable
                }
            }
            .map { preferences ->
                val rememberMe = preferences[Keys.REMEMBER_ME] ?: false
                if (!rememberMe) return@map null

                val email = preferences[Keys.EMAIL].orEmpty()
                val password = preferences[Keys.PASSWORD].orEmpty()
                if (email.isBlank() || password.isBlank()) return@map null

                RememberedCredential(email = email, password = password)
            }

    override suspend fun saveRememberedCredential(email: String, password: String) {
        context.authCredentialDataStore.edit { preferences ->
            preferences[Keys.REMEMBER_ME] = true
            preferences[Keys.EMAIL] = email
            preferences[Keys.PASSWORD] = password
        }
    }

    override suspend fun clearRememberedCredential() {
        context.authCredentialDataStore.edit { preferences ->
            preferences[Keys.REMEMBER_ME] = false
            preferences.remove(Keys.EMAIL)
            preferences.remove(Keys.PASSWORD)
        }
    }

    private object Keys {
        val REMEMBER_ME = booleanPreferencesKey("remember_me")
        val EMAIL = stringPreferencesKey("email")
        val PASSWORD = stringPreferencesKey("password")
    }
}
