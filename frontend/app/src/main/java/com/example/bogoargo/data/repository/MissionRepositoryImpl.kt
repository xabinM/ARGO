package com.example.bogoargo.data.repository

import android.content.Context
import com.example.bogoargo.data.api.AuthApiService
import com.example.bogoargo.data.api.MissionApiService
import com.example.bogoargo.data.cache.MissionCache
import com.example.bogoargo.data.mapper.MissionProblemMapper
import com.example.bogoargo.domain.model.*
import com.example.bogoargo.domain.repository.IMissionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MissionRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val missionApiService: MissionApiService,
    private val context: Context? = null
) : IMissionRepository {
    private val missionCache = context?.let { MissionCache(it) }
    override suspend fun getMissions(): DataResult<List<Mission>> {
        return withContext(Dispatchers.IO) {
            try {
                // TODO: Implement proper API call for getMissions
                DataResult.Success(emptyList())
            } catch (e: Exception) {
                DataResult.Error(DataException.UnknownError(e.message ?: "Failed to get missions"))
            }
        }
    }
    
    override suspend fun getMissionById(missionId: Long): DataResult<Mission> {
        return withContext(Dispatchers.IO) {
            try {
                // TODO: Implement proper API call for getMissionById
                DataResult.Error(DataException.NotFoundError)
            } catch (e: Exception) {
                DataResult.Error(DataException.UnknownError(e.message ?: "Failed to get mission"))
            }
        }
    }
    
    override suspend fun startMission(missionId: Long): DataResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // TODO: Implement proper API call for startMission
                DataResult.Success(Unit)
            } catch (e: Exception) {
                DataResult.Error(DataException.UnknownError(e.message ?: "Failed to start mission"))
            }
        }
    }
    
    override suspend fun completeMission(missionId: Long): DataResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // TODO: Implement proper API call for completeMission
                DataResult.Success(Unit)
            } catch (e: Exception) {
                DataResult.Error(DataException.UnknownError(e.message ?: "Failed to complete mission"))
            }
        }
    }
    
    override suspend fun getMissionProgress(missionId: Long): DataResult<Int> {
        return withContext(Dispatchers.IO) {
            try {
                // TODO: Implement proper API call for getMissionProgress
                DataResult.Success(0)
            } catch (e: Exception) {
                DataResult.Error(DataException.UnknownError(e.message ?: "Failed to get mission progress"))
            }
        }
    }
    
    suspend fun getMissionSpots(classId: Long): DataResult<List<MissionSpot>> {
        return withContext(Dispatchers.IO) {
            // 캐시된 데이터가 있고 유효한지 확인
            missionCache?.let { cache ->
                val cachedSpots = cache.getCachedMissionSpots(classId).first()
                if (cachedSpots != null) {
                    return@withContext DataResult.Success(cachedSpots)
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
                        DataResult.Success(body.data)
                    } else {
                        DataResult.Error(DataException.UnknownError(body?.message ?: "Unknown error"))
                    }
                } else {
                    when (response.code()) {
                        403 -> DataResult.Error(DataException.UnauthorizedError)
                        404 -> DataResult.Error(DataException.NotFoundError)
                        else -> DataResult.Error(DataException.NetworkError)
                    }
                }
            } catch (e: Exception) {
                DataResult.Error(DataException.UnknownError(e.message ?: "Failed to get mission spots"))
            }
        }
    }

    // 새로운 미션 생성
    override suspend fun createMission(teamId: Long, spotId: Long): DataResult<MissionCreateResult> {
        return withContext(Dispatchers.IO) {
            try {
                val response = missionApiService.createMission(teamId, spotId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val result = MissionProblemMapper.mapToMissionCreateResult(body)
                        DataResult.Success(result)
                    } else {
                        DataResult.Error(DataException.UnknownError("Empty response body"))
                    }
                } else {
                    when (response.code()) {
                        403 -> DataResult.Error(DataException.UnauthorizedError)
                        404 -> DataResult.Error(DataException.NotFoundError)
                        else -> DataResult.Error(DataException.NetworkError)
                    }
                }
            } catch (e: Exception) {
                DataResult.Error(DataException.UnknownError(e.message ?: "Failed to create mission"))
            }
        }
    }

    // 퀴즈 미션 제출
    override suspend fun submitQuizMission(missionId: Long, isSuccess: Boolean): DataResult<MissionSubmitResult> {
        return withContext(Dispatchers.IO) {
            try {
                val request = MissionProblemMapper.mapToMissionSubmitRequest(isSuccess)
                val response = missionApiService.submitMission(missionId, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val result = MissionProblemMapper.mapToMissionSubmitResult(body)
                        DataResult.Success(result)
                    } else {
                        DataResult.Error(DataException.UnknownError("Empty response body"))
                    }
                } else {
                    when (response.code()) {
                        403 -> DataResult.Error(DataException.UnauthorizedError)
                        404 -> DataResult.Error(DataException.NotFoundError)
                        else -> DataResult.Error(DataException.NetworkError)
                    }
                }
            } catch (e: Exception) {
                DataResult.Error(DataException.UnknownError(e.message ?: "Failed to submit quiz mission"))
            }
        }
    }

    // TODO: 셀피 미션 제출 (백엔드 API 완성 후 구현)
    override suspend fun submitSelfieMission(missionId: Long, imageBase64: String, pose: String): DataResult<MissionSubmitResult> {
        return withContext(Dispatchers.IO) {
            try {
                // TODO: 실제 API 호출로 교체
                // val request = MissionProblemMapper.mapToSelfieMissionSubmitRequest(imageBase64, pose)
                // val response = missionApiService.submitSelfieMission(missionId, request)
                
                // 임시로 성공 응답 반환
                DataResult.Success(MissionSubmitResult(successful = true, cardId = 1L, tier = "COMMON"))
            } catch (e: Exception) {
                DataResult.Error(DataException.UnknownError(e.message ?: "Failed to submit selfie mission"))
            }
        }
    }

    // TODO: 셀피 검증 (백엔드 API 완성 후 구현)
    override suspend fun validateSelfie(imageBase64: String, pose: String): DataResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // TODO: 실제 API 호출로 교체
                // val request = MissionProblemMapper.mapToSelfieMissionSubmitRequest(imageBase64, pose)
                // val response = missionApiService.validateSelfie(request)
                
                // 임시로 성공 응답 반환 (80% 확률로 성공)
                val isValid = (0..100).random() < 80
                DataResult.Success(isValid)
            } catch (e: Exception) {
                DataResult.Error(DataException.UnknownError(e.message ?: "Failed to validate selfie"))
            }
        }
    }
}