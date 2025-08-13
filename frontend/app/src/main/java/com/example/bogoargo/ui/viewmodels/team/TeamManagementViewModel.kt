package com.example.bogoargo.ui.viewmodels.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.dto.response.TeamAssignResponse
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.Team
import com.example.bogoargo.domain.model.User
import com.example.bogoargo.domain.use_case.team.ManageTeamUseCase
import com.example.bogoargo.domain.use_case.team.CreateTeamUseCase
import com.example.bogoargo.domain.use_case.classroom.GetCompleteClassDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
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
    val createSuccess: Boolean = false,
    val teams: List<Team> = emptyList(),
    val className: String = ""
)

@HiltViewModel
class TeamManagementViewModel @Inject constructor(
    private val manageTeamUseCase: ManageTeamUseCase,
    private val createTeamUseCase: CreateTeamUseCase,
    private val getCompleteClassDetailUseCase: GetCompleteClassDetailUseCase
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

    fun createTeam(classId: Long, teamName: String, maxMembers: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = createTeamUseCase(classId, teamName, maxMembers)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        createSuccess = true,
                        createdTeam = result.data
                    )
                    // 팀 생성 후 팀 리스트 다시 로드
                    loadClassDetail(classId)
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

    fun loadClassDetail(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = getCompleteClassDetailUseCase(classId)) {
                is DataResult.Success -> {
                    val classDetailResponse = result.data
                    // ClassDetailResponse의 teams 필드에서 Team 모델로 변환
                    val teams = classDetailResponse.teams?.map { teamDto ->
                        Team(
                            id = teamDto.teamId,
                            classId = classId,
                            name = teamDto.teamName,
                            maxMembers = teamDto.members.size + 3, // 현재 멤버 + 여유분
                            currentMembers = teamDto.memberCount,
                            createdAt = java.time.LocalDate.now(), // 기본값
                            totalPoints = teamDto.totalScore
                        )
                    } ?: emptyList()
                    
                    // ClassDetailResponse의 data.classInfo에서 className 추출
                    val className = classDetailResponse.data?.classInfo?.className ?: ""
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        teams = teams,
                        className = className,
                        errorMessage = null
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