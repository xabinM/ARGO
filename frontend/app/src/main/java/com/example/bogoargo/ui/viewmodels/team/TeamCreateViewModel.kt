package com.example.bogoargo.ui.viewmodels.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.model.Team
import com.example.bogoargo.data.repository.AuthRepository
import com.example.bogoargo.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeamCreateUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val createdTeam: Team? = null
)

class TeamCreateViewModel @Inject constructor(
    private val teamRepository: TeamRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamCreateUiState())
    val uiState: StateFlow<TeamCreateUiState> = _uiState

    fun createTeam(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            teamRepository.createTeam(classId).fold(
                onSuccess = { createdTeam ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        createdTeam = createdTeam
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
        _uiState.value = TeamCreateUiState()
    }
}