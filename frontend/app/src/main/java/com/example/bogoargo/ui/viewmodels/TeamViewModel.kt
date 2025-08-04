package com.example.bogoargo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.dto.response.TeamAssignResponse
import com.example.bogoargo.data.model.Team
import com.example.bogoargo.data.repository.TeamRepository
import com.example.bogoargo.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class TeamViewModel @Inject constructor(
    private val teamRepository: TeamRepository
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _createdTeam = MutableStateFlow<Team?>(null)
    val createdTeam: StateFlow<Team?> = _createdTeam.asStateFlow()

    private val _assignResponse = MutableStateFlow<TeamAssignResponse?>(null)
    val assignResponse: StateFlow<TeamAssignResponse?> = _assignResponse.asStateFlow()

    private val _deletedTeamStudents = MutableStateFlow<List<User>?>(null)
    val deletedTeamStudents: StateFlow<List<User>?> = _deletedTeamStudents.asStateFlow()

    fun clearUiState() {
        _uiState.value = UiState.Idle
    }

    fun createTeam(classId: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = teamRepository.createTeam(classId)
                result.fold(
                    onSuccess = { team ->
                        _createdTeam.value = team
                        _uiState.value = UiState.Success
                    },
                    onFailure = { exception ->
                        _uiState.value = UiState.Error(exception.message ?: "팀 생성 실패")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun assignTeam(classId: Long, teamId: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = teamRepository.assignTeam(classId, teamId)
                result.fold(
                    onSuccess = { assignResponse ->
                        _assignResponse.value = assignResponse
                        _uiState.value = UiState.Success
                    },
                    onFailure = { exception ->
                        _uiState.value = UiState.Error(exception.message ?: "팀 배정 실패")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun assignTeamRandom(classId: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = teamRepository.assignTeamRandom(classId)
                result.fold(
                    onSuccess = { assignResponse ->
                        _assignResponse.value = assignResponse
                        _uiState.value = UiState.Success
                    },
                    onFailure = { exception ->
                        _uiState.value = UiState.Error(exception.message ?: "랜덤 팀 배정 실패")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun deleteTeam(classId: Long, teamId: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = teamRepository.deleteTeam(classId, teamId)
                result.fold(
                    onSuccess = { students ->
                        _deletedTeamStudents.value = students
                        _uiState.value = UiState.Success
                    },
                    onFailure = { exception ->
                        _uiState.value = UiState.Error(exception.message ?: "팀 삭제 실패")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun clearCreatedTeam() {
        _createdTeam.value = null
    }

    fun clearAssignResponse() {
        _assignResponse.value = null
    }

    fun clearDeletedTeamStudents() {
        _deletedTeamStudents.value = null
    }
}