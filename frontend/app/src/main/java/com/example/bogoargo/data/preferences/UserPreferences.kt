package com.example.bogoargo.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bogoargo.domain.model.User
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UserPreferences @Inject constructor (
    @ApplicationContext private val context: Context
){
    private companion object {
        private const val USER_PREFS_NAME = "user_prefs"
        // DataStore 생성 시 Context 확장 함수 사용
        private val Context.dataStore by preferencesDataStore(name = USER_PREFS_NAME)

        private val USER_INFO_KEY = stringPreferencesKey("user_info")
        private val gson = Gson()
    }

    suspend fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        context.dataStore.edit { prefs ->
            prefs[USER_INFO_KEY] = userJson
        }
    }

    suspend fun getUser(): User? {
        val prefs = context.dataStore.data.first()
        return prefs[USER_INFO_KEY]?.let {
            gson.fromJson(it, User::class.java)
        }
    }

    suspend fun clearUser() {
        context.dataStore.edit { it.clear() }
    }
}
