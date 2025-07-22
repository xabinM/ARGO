package com.example.bogoargo.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bogoargo.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property to get DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserRepository(private val context: Context) {
    
    // Preference keys
    private companion object {
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_BIO = stringPreferencesKey("user_bio")
        val USER_PROFILE_PICTURE = stringPreferencesKey("user_profile_picture")
    }
    
    // Get user data as Flow
    val userFlow: Flow<User> = context.dataStore.data.map { preferences ->
        User(
            id = preferences[USER_ID] ?: "user_123",
            userName = preferences[USER_NAME] ?: "John Doe",
            email = preferences[USER_EMAIL] ?: "john.doe@example.com",
            bio = preferences[USER_BIO] ?: "Android Developer | Kotlin Enthusiast",
            profilePictureUrl = preferences[USER_PROFILE_PICTURE]
        )
    }
    
    // Save user data
    suspend fun saveUser(user: User) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = user.id
            preferences[USER_NAME] = user.userName
            preferences[USER_EMAIL] = user.email
            preferences[USER_BIO] = user.bio
            user.profilePictureUrl?.let {
                preferences[USER_PROFILE_PICTURE] = it
            }
        }
    }
    
    // Update specific user fields
    suspend fun updateUserProfile(userName: String, bio: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = userName
            preferences[USER_BIO] = bio
        }
    }
    
    // Clear all user data
    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}