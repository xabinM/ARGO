package com.example.bogoargo.data.repository

import android.content.Context
import android.util.Base64
import com.example.bogoargo.data.api.ClassApiService
import com.example.bogoargo.data.api.MissionApiService
import com.example.bogoargo.data.api.ProblemApiService
import com.example.bogoargo.data.cache.MissionCache
import com.example.bogoargo.data.mapper.MissionProblemMapper
import com.example.bogoargo.domain.model.*
import com.example.bogoargo.domain.repository.IMissionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class MissionRepositoryImpl @Inject constructor(
    private val classApiService: ClassApiService,
    private val missionApiService: MissionApiService,
    private val problemApiService: ProblemApiService,
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
                val response = classApiService.getSpots(classId)
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
                        if (result != null) {
                            DataResult.Success(result)
                        } else {
                            DataResult.Error(DataException.UnknownError("Failed to parse problem detail"))
                        }
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

    // 셀피 검증
    override suspend fun validateSelfie(teamId: Long, imageBase64: String, pose: String): DataResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Base64 문자열을 바이트 배열로 변환
                val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                
                // MultipartBody.Part 생성
                val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaType())
                val imagePart = MultipartBody.Part.createFormData("image", "selfie.jpg", requestBody)
                
                // pose를 RequestBody로 변환
                val posePart = pose.toRequestBody("text/plain".toMediaType())
                
                // API 호출
                val response = problemApiService.determineSelfiePose(teamId, imagePart, posePart)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        DataResult.Success(body.result.success)
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
                DataResult.Error(DataException.UnknownError(e.message ?: "Failed to validate selfie"))
            }
        }
    }

    // 미션 지점 가능 여부 확인
    override suspend fun checkPossibleMissionSpot(teamId: Long, spotId: Long): DataResult<MissionPossibleCheckResult> {
        return withContext(Dispatchers.IO) {
            try {
                val response = missionApiService.checkPossibleMissionSpot(teamId, spotId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val result = MissionProblemMapper.mapToMissionPossibleCheckResult(body)
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
                DataResult.Error(DataException.UnknownError(e.message ?: "Failed to check mission possibility"))
            }
        }
    }
}