package com.example.bogoargo.data.repository

import android.content.Context
import com.example.bogoargo.data.api.AuthApiService
import com.example.bogoargo.data.cache.MissionCache
import com.example.bogoargo.data.model.MissionSpot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class MissionRepository(
    private val apiService: AuthApiService,
    private val context: Context? = null
) {
    private val missionCache = context?.let { MissionCache(it) }
    suspend fun getMissionSpots(classId: Long): Result<List<MissionSpot>> {
        return withContext(Dispatchers.IO) {
            // 캐시된 데이터가 있고 유효한지 확인
            missionCache?.let { cache ->
                val cachedSpots = cache.getCachedMissionSpots(classId).first()
                if (cachedSpots != null) {
                    return@withContext Result.success(cachedSpots)
                }
            }
            
            // 캐시가 없거나 유효하지 않으면 API 호출
            try {
                val response = apiService.getMissionSpots(classId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        // 성공적으로 데이터를 받으면 캐시에 저장
                        missionCache?.cacheMissionSpots(classId, body.data)
                        Result.success(body.data)
                    } else {
                        Result.failure(Exception(body?.message ?: "Unknown error"))
                    }
                } else {
                    when (response.code()) {
                        403 -> Result.failure(Exception("반 소속 아님"))
                        404 -> Result.failure(Exception("반 ID 존재하지 않음"))
                        else -> Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}