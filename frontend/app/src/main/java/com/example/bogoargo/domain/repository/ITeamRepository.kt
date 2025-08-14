package com.example.bogoargo.domain.repository

import com.example.bogoargo.data.dto.response.TeamAssignResponse
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.Team
import com.example.bogoargo.domain.model.User

interface ITeamRepository {
    suspend fun createTeam(classId: Long, teamName: String, maxMembers: Int): DataResult<Team>
    suspend fun getTeams(): DataResult<List<Team>>
    suspend fun getTeamById(teamId: Long): DataResult<Team>
    suspend fun updateTeam(teamId: Long): DataResult<Team>
    suspend fun deleteTeam(teamId: Long): DataResult<Unit>
    suspend fun deleteTeam(classId: Long, teamId: Long): DataResult<List<User>>
    suspend fun joinTeam(teamId: Long): DataResult<Unit>
    suspend fun leaveTeam(teamId: Long): DataResult<Unit>
    suspend fun assignTeam(classId: Long, teamId: Long): DataResult<TeamAssignResponse>
    suspend fun assignTeamRandom(classId: Long): DataResult<TeamAssignResponse>
}