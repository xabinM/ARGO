package com.example.bogoargo.data.cache

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bogoargo.data.model.MissionSpot
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.missionDataStore: DataStore<Preferences> by preferencesDataStore(name = "mission_cache")

class MissionCache(private val context: Context) {
    
    private val gson = Gson()
    
    companion object {
        private val MISSION_DATA_KEY = stringPreferencesKey("mission_data")
        private val LAST_UPDATE_KEY = longPreferencesKey("last_update")
        private val CLASS_ID_KEY = longPreferencesKey("cached_class_id")
        
        // 캐시 유효 시간 (1시간)
        private const val CACHE_VALIDITY_HOURS = 1
        private const val CACHE_VALIDITY_MS = CACHE_VALIDITY_HOURS * 60 * 60 * 1000L
    }
    
    suspend fun cacheMissionSpots(classId: Long, spots: List<MissionSpot>) {
        val spotsJson = gson.toJson(spots)
        val currentTime = System.currentTimeMillis()
        
        context.missionDataStore.edit { preferences ->
            preferences[MISSION_DATA_KEY] = spotsJson
            preferences[LAST_UPDATE_KEY] = currentTime
            preferences[CLASS_ID_KEY] = classId
        }
    }
    
    fun getCachedMissionSpots(classId: Long): Flow<List<MissionSpot>?> {
        return context.missionDataStore.data.map { preferences ->
            val cachedClassId = preferences[CLASS_ID_KEY]
            val lastUpdate = preferences[LAST_UPDATE_KEY] ?: 0L
            val currentTime = System.currentTimeMillis()
            
            // 같은 반이고 캐시가 유효한 경우만 반환
            if (cachedClassId == classId && (currentTime - lastUpdate) < CACHE_VALIDITY_MS) {
                val spotsJson = preferences[MISSION_DATA_KEY]
                if (spotsJson != null) {
                    try {
                        val type = object : TypeToken<List<MissionSpot>>() {}.type
                        gson.fromJson<List<MissionSpot>>(spotsJson, type)
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }
            } else {
                null
            }
        }
    }
    
    suspend fun isCacheValid(classId: Long): Boolean {
        return context.missionDataStore.data.map { preferences ->
            val cachedClassId = preferences[CLASS_ID_KEY]
            val lastUpdate = preferences[LAST_UPDATE_KEY] ?: 0L
            val currentTime = System.currentTimeMillis()
            
            cachedClassId == classId && (currentTime - lastUpdate) < CACHE_VALIDITY_MS
        }.let { flow ->
            // Flow를 단일 값으로 변환
            var result = false
            flow.collect { result = it }
            result
        }
    }
    
    suspend fun clearCache() {
        context.missionDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}