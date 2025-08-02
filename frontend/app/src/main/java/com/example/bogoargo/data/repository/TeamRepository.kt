package com.example.bogoargo.data.repository

import com.example.bogoargo.data.api.TeamApiService
import com.example.bogoargo.data.dto.TeamCreateRequest
import com.example.bogoargo.data.dto.TeamAssignStudentRequest
import com.example.bogoargo.data.dto.TeatAssignRamdomRequest
import com.example.bogoargo.data.dto.response.TeamCreateResponse
import com.example.bogoargo.data.dto.response.TeamAssignResponse
import com.example.bogoargo.data.dto.response.TeamDeleteResponse
import com.example.bogoargo.data.mapper.toDomainModel
import com.example.bogoargo.data.model.Team
import com.example.bogoargo.domain.model.User

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamRepository @Inject constructor(
    private val teamApiService: TeamApiService
) {
    
    suspend fun createTeam(classId: Long): Result<Team?> {
        return try {
            val response = teamApiService.createTeam(classId)
            if (response.isSuccessful) {
                val teamCreateResponse = response.body()
                if (teamCreateResponse?.success == true && teamCreateResponse.data != null) {
                    Result.success(teamCreateResponse.data.toDomainModel())
                } else {
                    Result.failure(Exception(teamCreateResponse?.message ?: "팀 생성에 실패했습니다."))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun assignTeam(classId: Long, teamId: Long): Result<TeamAssignResponse> {
        return try {
            val response = teamApiService.AssignTeam(classId, teamId)
            if (response.isSuccessful) {
                val assignResponse = response.body()
                if (assignResponse?.success == true) {
                    Result.success(assignResponse)
                } else {
                    Result.failure(Exception(assignResponse?.message ?: "팀 배정에 실패했습니다."))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun assignTeamRandom(classId: Long): Result<TeamAssignResponse> {
        return try {
            val response = teamApiService.AssignTeamRandom(classId)
            if (response.isSuccessful) {
                val assignResponse = response.body()
                if (assignResponse?.success == true) {
                    Result.success(assignResponse)
                } else {
                    Result.failure(Exception(assignResponse?.message ?: "랜덤 팀 배정에 실패했습니다."))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteTeam(classId: Long, teamId: Long): Result<List<User>?> {
        return try {
            val response = teamApiService.deleteTeam(classId, teamId)
            if (response.isSuccessful) {
                val deleteResponse = response.body()
                if (deleteResponse?.success == true) {
                    // TeamDeleteResponse의 students를 User 모델로 변환 필요시 추가
                    Result.success(null) // 현재는 UserDataDto -> User 매퍼가 없어서 null 반환
                } else {
                    Result.failure(Exception(deleteResponse?.message ?: "팀 삭제에 실패했습니다."))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}