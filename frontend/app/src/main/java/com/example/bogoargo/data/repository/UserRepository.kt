package com.example.bogoargo.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bogoargo.data.model.User
import com.example.bogoargo.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property to get DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserRepository(private val context: Context) {
    
    // Preference keys
    private companion object {
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_FULL_NAME = stringPreferencesKey("user_full_name")
        val USER_ROLE = stringPreferencesKey("user_role")
    }
    
    // Get user data as Flow
    val userFlow: Flow<User> = context.dataStore.data.map { preferences ->
        User(
            userId = preferences[USER_ID]?.toLongOrNull() ?: 0L,
            username = preferences[USER_NAME] ?: "user123",
            name = preferences[USER_FULL_NAME] ?: "John Doe",
            role = preferences[USER_ROLE]?.let { UserRole.valueOf(it) } ?: UserRole.STUDENT
        )
    }
    
    // Save user data
    suspend fun saveUser(user: User) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = user.userId.toString()
            preferences[USER_NAME] = user.username
            preferences[USER_FULL_NAME] = user.name
            preferences[USER_ROLE] = user.role.name
        }
    }
    
    // Update specific user fields
    suspend fun updateUserProfile(username: String, name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = username
            preferences[USER_FULL_NAME] = name
        }
    }
    
    // Clear all user data
    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}