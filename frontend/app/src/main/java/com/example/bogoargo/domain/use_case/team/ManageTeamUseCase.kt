package com.example.bogoargo.domain.use_case.team

import com.example.bogoargo.data.dto.response.TeamAssignResponse
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.User
import com.example.bogoargo.domain.repository.ITeamRepository
import javax.inject.Inject

class ManageTeamUseCase @Inject constructor(
    private val teamRepository: ITeamRepository
) {
    suspend fun assignTeam(classId: Long, teamId: Long): DataResult<TeamAssignResponse> {
        return teamRepository.assignTeam(classId, teamId)
    }

    suspend fun assignTeamRandom(classId: Long): DataResult<TeamAssignResponse> {
        return teamRepository.assignTeamRandom(classId)
    }

    suspend fun deleteTeam(classId: Long, teamId: Long): DataResult<List<User>> {
        return teamRepository.deleteTeam(classId, teamId)
    }
}