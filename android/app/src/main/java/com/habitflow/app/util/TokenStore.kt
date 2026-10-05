package com.habitflow.app.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("habitflow_secure")

class TokenStore(private val context: Context) {
    private val accessKey = stringPreferencesKey("access_token")
    private val refreshKey = stringPreferencesKey("refresh_token")
    private val userJsonKey = stringPreferencesKey("user_json")

    val accessTokenFlow: Flow<String?> = context.dataStore.data.map { it[accessKey] }

    suspend fun getAccessToken(): String? =
        context.dataStore.data.map { it[accessKey] }.first()

    suspend fun getRefreshToken(): String? =
        context.dataStore.data.map { it[refreshKey] }.first()

    suspend fun saveTokens(access: String, refresh: String) {
        context.dataStore.edit {
            it[accessKey] = access
            it[refreshKey] = refresh
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun isLoggedIn(): Boolean = !getAccessToken().isNullOrBlank()
}
