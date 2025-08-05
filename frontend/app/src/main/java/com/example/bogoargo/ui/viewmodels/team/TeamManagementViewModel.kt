package com.example.bogoargo.ui.viewmodels.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.dto.response.TeamAssignResponse
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.Team
import com.example.bogoargo.domain.model.User
import com.example.bogoargo.domain.use_case.team.ManageTeamUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
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

@HiltViewModel
class TeamManagementViewModel @Inject constructor(
    private val manageTeamUseCase: ManageTeamUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamManagementUiState())
    val uiState: StateFlow<TeamManagementUiState> = _uiState

    fun assignTeam(classId: Long, teamId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = manageTeamUseCase.assignTeam(classId, teamId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        assignSuccess = true,
                        assignResponse = result.data
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // Already set loading state
                }
            }
        }
    }

    fun assignTeamRandom(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = manageTeamUseCase.assignTeamRandom(classId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        randomAssignSuccess = true,
                        assignResponse = result.data
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // Already set loading state
                }
            }
        }
    }

    fun deleteTeam(classId: Long, teamId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = manageTeamUseCase.deleteTeam(classId, teamId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        deleteSuccess = true,
                        deletedStudents = result.data
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // Already set loading state
                }
            }
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