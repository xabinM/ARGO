package com.example.bogoargo.ui.viewmodels.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.Team
import com.example.bogoargo.domain.use_case.team.CreateTeamUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class TeamCreateUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val createdTeam: Team? = null
)

@HiltViewModel
class TeamCreateViewModel @Inject constructor(
    private val createTeamUseCase: CreateTeamUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamCreateUiState())
    val uiState: StateFlow<TeamCreateUiState> = _uiState

    fun createTeam(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = createTeamUseCase(classId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        createdTeam = result.data
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
        _uiState.value = TeamCreateUiState()
    }
}