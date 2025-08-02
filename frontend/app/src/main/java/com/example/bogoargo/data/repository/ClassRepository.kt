package com.example.bogoargo.data.repository

import com.example.bogoargo.data.api.ClassApiService
import com.example.bogoargo.data.dto.ClassCreateRequest
import com.example.bogoargo.data.dto.ApproveStudentRequest
import com.example.bogoargo.data.dto.ApplyClassRequest
import com.example.bogoargo.data.response.ClassDataDto
import com.example.bogoargo.data.response.ClassListResponse
import com.example.bogoargo.data.response.ClassDetailResponse
import com.example.bogoargo.data.response.ClassMemberResponse
import com.example.bogoargo.data.response.ClassLeaveResponse
import com.example.bogoargo.data.response.applyClassResponse

import com.example.bogoargo.data.mapper.toDomainModel
import com.example.bogoargo.data.model.Class
import com.example.bogoargo.data.dto.response.ApplicationResponseDto
import com.example.bogoargo.data.dto.response.MessageResponseDto
import com.example.bogoargo.data.dto.response.UserDataDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassRepository @Inject constructor(
    private val classApiService: ClassApiService
) {
    
    // 교사용 반 목록 조회
    suspend fun getTeacherClassList(
        page: Int = 1,
        size: Int = 10,
        status: String? = "active"
    ): Result<List<Class>> {
        return try {
            val response = classApiService.getTeacherClassList(page, size, status)
            if (response.isSuccessful) {
                val classListResponse = response.body()
                if (classListResponse?.success == true && classListResponse.data != null) {
                    val classes = classListResponse.data.map { it.toDomainModel() }
                    Result.success(classes)
                } else {
                    Result.failure(Exception(classListResponse?.message ?: "반 목록을 불러올 수 없습니다."))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 학생용 반 목록 조회
    suspend fun getStudentClassList(
        page: Int = 1,
        size: Int = 10,
        status: String? = "active"
    ): Result<List<Class>> {
        return try {
            val response = classApiService.getStudentClassList(page, size, status)
            if (response.isSuccessful) {
                val classListResponse = response.body()
                if (classListResponse?.success == true && classListResponse.data != null) {
                    val classes = classListResponse.data.map { it.toDomainModel() }
                    Result.success(classes)
                } else {
                    Result.failure(Exception(classListResponse?.message ?: "반 목록을 불러올 수 없습니다."))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 반 상세 정보 조회
    suspend fun getClassDetail(classId: Long): Result<Class> {
        return try {
            val response = classApiService.getClassDetail(classId)
            if (response.isSuccessful) {
                val classData = response.body()
                if (classData != null) {
                    Result.success(classData.toDomainModel())
                } else {
                    Result.failure(Exception("반 정보를 불러올 수 없습니다."))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 반 생성 (교사 기능)
    suspend fun createClass(classCreateRequest: ClassCreateRequest): Result<Class?> {
        return try {
            val response = classApiService.createClass(classCreateRequest)
            if (response.isSuccessful) {
                val classDetailResponse = response.body()
                if (classDetailResponse?.success == true && classDetailResponse.data != null) {
                    Result.success(classDetailResponse.data.toDomainModel())
                } else {
                    Result.failure(Exception(classDetailResponse?.message ?: "반 생성에 실패했습니다."))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 참여 신청한 학생 목록 조회 (교사 기능)
    suspend fun getApplicationList(classId: Long): Result<ApplicationResponseDto?> {
        return try {
            val response = classApiService.getApplicationList(classId)
            if (response.isSuccessful) {
                val applicationResponse = response.body()
                Result.success(applicationResponse)
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 참여 신청 승인/거절 (교사 기능)
    suspend fun approveApplication(classId: Long, applicationId: Long): Result<MessageResponseDto?> {
        return try {
            val response = classApiService.approveApplication(classId, applicationId)
            if (response.isSuccessful) {
                val successResponse = response.body()
                if (successResponse?.success == true) {
                    Result.success(successResponse)
                } else {
                    Result.failure(Exception(successResponse?.message ?: "신청 처리에 실패했습니다."))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 반 소속 학생 목록 조회 (교사 기능)
    suspend fun getClassMemberList(
        classId: Long,
        status: String,
        page: Int = 10,
        size: Int = 10
    ): Result<List<UserDataDto>?> {
        return try {
            val response = classApiService.getClassMemberList(classId, status, page, size)
            if (response.isSuccessful) {
                val memberResponse = response.body()
                if (memberResponse?.success == true) {
                    Result.success(memberResponse.data)
                } else {
                    Result.failure(Exception(memberResponse?.message ?: "학생 목록을 불러올 수 없습니다."))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 반 삭제 (교사 기능)
    suspend fun deleteClass(classId: Long): Result<MessageResponseDto?> {
        return try {
            val response = classApiService.deleteClass(classId)
            if (response.isSuccessful) {
                val messageResponse = response.body()
                if (messageResponse?.success == true) {
                    Result.success(messageResponse)
                } else {
                    Result.failure(Exception(messageResponse?.message ?: "반 삭제에 실패했습니다."))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 반 참여 신청 (학생 기능)
    suspend fun applyClass(inviteCode: String): Result<applyClassResponse?> {
        return try {
            val response = classApiService.applyClass(inviteCode)
            if (response.isSuccessful) {
                val applyResponse = response.body()
                if (applyResponse?.success == true) {
                    Result.success(applyResponse)
                } else {
                    Result.failure(Exception(applyResponse?.message ?: "반 참여 신청에 실패했습니다."))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 반 탈퇴 (학생 기능)
    suspend fun leaveClass(classId: Long): Result<ClassLeaveResponse?> {
        return try {
            val response = classApiService.leaveClass(classId)
            if (response.isSuccessful) {
                val leaveResponse = response.body()
                if (leaveResponse?.success == true) {
                    Result.success(leaveResponse)
                } else {
                    Result.failure(Exception(leaveResponse?.message ?: "반 탈퇴에 실패했습니다."))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}