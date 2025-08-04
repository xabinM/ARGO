package com.example.bogoargo.ui.viewmodels.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.dto.response.TeamAssignResponse
import com.example.bogoargo.data.model.Team
import com.example.bogoargo.data.model.User
import com.example.bogoargo.data.repository.AuthRepository
import com.example.bogoargo.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeamManagementUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val assignSuccess: Boolean = false,
    val assignResponse: TeamAssignResponse? = null,
    val randomAssignSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
    val deletedStudents: List<User>? = null,
    val createdTeam: Team? = null,
    val createSuccess: Boolean = false
)

class TeamManagementViewModel @Inject constructor(
    private val teamRepository: TeamRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamManagementUiState())
    val uiState: StateFlow<TeamManagementUiState> = _uiState

    fun assignTeam(classId: Long, teamId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            teamRepository.assignTeam(classId, teamId).fold(
                onSuccess = { assignResponse ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        assignSuccess = true,
                        assignResponse = assignResponse
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
            )
        }
    }

    fun assignTeamRandom(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            teamRepository.assignTeamRandom(classId).fold(
                onSuccess = { assignResponse ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        randomAssignSuccess = true,
                        assignResponse = assignResponse
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
            )
        }
    }

    fun deleteTeam(classId: Long, teamId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            teamRepository.deleteTeam(classId, teamId).fold(
                onSuccess = { deletedStudents ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        deleteSuccess = true,
                        deletedStudents = deletedStudents
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
            )
        }
    }

    fun clearState() {
        _uiState.value = TeamManagementUiState()
    }

    fun clearSuccessFlags() {
        _uiState.value = _uiState.value.copy(
            assignSuccess = false,
            randomAssignSuccess = false,
            deleteSuccess = false,
            createSuccess = false,
            assignResponse = null,
            deletedStudents = null,
            createdTeam = null
        )
    }
}