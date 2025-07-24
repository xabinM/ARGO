package com.example.bogoargo.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bogoargo.data.model.TokenInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

class AuthRepository(private val context: Context) {
    
    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }
    
    val tokenFlow: Flow<TokenInfo?> = context.authDataStore.data.map { preferences ->
        val accessToken = preferences[ACCESS_TOKEN]
        val refreshToken = preferences[REFRESH_TOKEN]
        
        if (accessToken != null && refreshToken != null) {
            TokenInfo(accessToken, refreshToken)
        } else {
            null
        }
    }
    
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.authDataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
        }
    }
    
    suspend fun clearTokens() {
        context.authDataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
        }
    }
    
    suspend fun getAccessToken(): String? {
        var token: String? = null
        context.authDataStore.data.collect { preferences ->
            token = preferences[ACCESS_TOKEN]
        }
        return token
    }
    
    suspend fun getRefreshToken(): String? {
        var token: String? = null
        context.authDataStore.data.collect { preferences ->
            token = preferences[REFRESH_TOKEN]
        }
        return token
    }
}