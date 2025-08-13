package com.example.bogoargo.domain.use_case.team

import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.Team
import com.example.bogoargo.domain.repository.ITeamRepository
import javax.inject.Inject

class CreateTeamUseCase @Inject constructor(
    private val teamRepository: ITeamRepository
) {
    suspend operator fun invoke(
        classId: Long,
        teamName: String,
        maxMembers: Int
    ): DataResult<Team> {
        return teamRepository.createTeam(classId, teamName, maxMembers)
    }
}