package com.example.bogoargo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.model.Team
import com.example.bogoargo.data.model.TeamMember
import com.example.bogoargo.data.model.User
import com.example.bogoargo.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeamViewModel(
    private val repository: TeamRepository = TeamRepository()
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()

    private val _teamMembers = MutableStateFlow<List<TeamMember>>(emptyList())
    val teamMembers: StateFlow<List<TeamMember>> = _teamMembers.asStateFlow()

    private val _availableStudents = MutableStateFlow<List<User>>(emptyList())
    val availableStudents: StateFlow<List<User>> = _availableStudents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadTeamsByClassId(classId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val teamsFromServer = repository.getTeamsByClassId(classId)
                _teams.value = teamsFromServer
            } catch (e: Exception) {
                _error.value = "팀 목록을 불러오는데 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createTeam(
        classId: String,
        name: String,
        description: String,
        maxMembers: Int,
        color: String
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            try {
                val teamData = Team(
                    classId = classId,
                    name = name,
                    description = description,
                    maxMembers = maxMembers,
                    color = color
                )
                val createdTeam = repository.createTeam(teamData)
                _teams.value = _teams.value + createdTeam
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error("팀 생성에 실패했습니다: ${e.message}")
            }
        }
    }

    fun updateTeam(team: Team) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val updatedTeam = repository.updateTeam(team)
                _teams.value = _teams.value.map {
                    if (it.id == updatedTeam.id) updatedTeam else it
                }
            } catch (e: Exception) {
                _error.value = "팀 수정에 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTeam(teamId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = repository.deleteTeam(teamId)
                if (success) {
                    _teams.value = _teams.value.filter { it.id != teamId }
                }
            } catch (e: Exception) {
                _error.value = "팀 삭제에 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTeamMembers(teamId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val membersFromServer = repository.getTeamMembers(teamId)
                _teamMembers.value = membersFromServer
            } catch (e: Exception) {
                _error.value = "팀 구성원을 불러오는데 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addMemberToTeam(teamId: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val newMember = repository.addMemberToTeam(teamId, userId)
                _teamMembers.value = _teamMembers.value + newMember
                
                // 팀의 현재 인원수 업데이트
                _teams.value = _teams.value.map { team ->
                    if (team.id == teamId) {
                        team.copy(currentMembers = team.currentMembers + 1)
                    } else team
                }
            } catch (e: Exception) {
                _error.value = "팀원 추가에 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeMemberFromTeam(teamId: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = repository.removeMemberFromTeam(teamId, userId)
                if (success) {
                    _teamMembers.value = _teamMembers.value.filter { it.userId != userId }
                    
                    // 팀의 현재 인원수 업데이트
                    _teams.value = _teams.value.map { team ->
                        if (team.id == teamId) {
                            team.copy(currentMembers = maxOf(0, team.currentMembers - 1))
                        } else team
                    }
                }
            } catch (e: Exception) {
                _error.value = "팀원 제거에 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAvailableStudents(classId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val studentsFromServer = repository.getAvailableStudents(classId)
                _availableStudents.value = studentsFromServer
            } catch (e: Exception) {
                _error.value = "학생 목록을 불러오는데 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }
}