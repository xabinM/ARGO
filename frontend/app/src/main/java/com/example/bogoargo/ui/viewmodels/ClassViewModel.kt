package com.example.bogoargo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.model.*
import com.example.bogoargo.data.repository.ClassRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar // Calendar 임포트 추가

class ClassViewModel(
    private val repository: ClassRepository,
    // ClassDetailModel과 TeamProgressListModel은 화면별 ViewModel로 가정하고
    // ClassViewModel에서 직접 StateFlow를 노출하기 보다는 각 모델이 필요한 곳에서
    // 사용되도록 하거나, ClassViewModel에서 필요한 데이터를 조합하여 노출하는 방식 고려
    // 여기서는 명확한 역할을 위해 ClassViewModel 내에서 직접적으로 다른 모델의 StateFlow를 노출하지 않음
) : ViewModel() {

    // --- UI 상태 관리 ---
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * 에러 상태 초기화 및 UI 상태를 Idle로 변경
     */
    fun clearUiState() {
        _uiState.value = UiState.Idle
    }

    // --- 데이터 상태 관리 ---
    private val _classes = MutableStateFlow<List<Class>>(emptyList())
    val classes: StateFlow<List<Class>> = _classes.asStateFlow()

    private val _members = MutableStateFlow<List<User>>(emptyList())
    val members: StateFlow<List<User>> = _members.asStateFlow()

    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()

    private val _missions = MutableStateFlow<List<Mission>>(emptyList())
    val missions: StateFlow<List<Mission>> = _missions.asStateFlow()

    private val _teamProgress = MutableStateFlow<List<TeamMissionProgress>>(emptyList())
    val teamProgress: StateFlow<List<TeamMissionProgress>> = _teamProgress.asStateFlow()

    // 초기 데이터 로드 (필요하다면)
    init {
        loadClasses()
    }

    // --- 비즈니스 로직 ---

    /**
     * 모든 반 목록 로드
     */
    fun loadClasses() = launchWithUiState {
        _classes.value = repository.getAllClasses()
    }

    /**
     * 새로운 반 생성
     */
    fun createClass(
        schoolName: String,
        className: String,
        maxStudents: Int,
        description: String,
        region: String
    ) = launchWithUiState {
        val classData = Class(
            id = "", // ID는 서버에서 생성될 수 있음
            year = Calendar.getInstance().get(Calendar.YEAR),
            schoolName = schoolName,
            className = className,
            description = description,
            region = region,
            invitationCode = "", // 초대 코드는 서버에서 생성될 수 있음
            maxStudents = maxStudents,
            currentStudents = 0
        )
        val createdClass = repository.createClass(classData)
        _classes.value += createdClass // 목록에 추가
    }

    /**
     * 특정 반의 멤버 목록 로드
     */
    fun loadMembersByClassId(classId: String) = launchWithUiState {
        _members.value = repository.getMembersByClassId(classId)
    }

    /**
     * 특정 반의 팀 목록 로드
     */
    fun loadTeamsByClassId(classId: String) = launchWithUiState {
        _teams.value = repository.getTeamsByClassId(classId)
    }

    /**
     * 새로운 팀 생성
     */
    fun createTeam(
        classId: String,
        name: String,
        description: String,
        maxMembers: Int,
        color: String
    ) = launchWithUiState {
        val team = Team(
            id = "", // ID는 서버에서 생성될 수 있음
            name = name,
            description = description,
            classId = classId,
            maxMembers = maxMembers,
            currentMembers = 0,
            color = color,
            createdAt = "10:00" //TODO: 현재 시간

        val createdTeam = repository.createTeam(team)
        _teams.value += createdTeam // 목록에 추가
    }

    /**
     * 팀 정보 업데이트
     */
    fun updateTeam(team: Team) = launchWithUiState {
        val updated = repository.updateTeam(team)
        _teams.value = _teams.value.map { if (it.id == updated.id) updated else it }
    }

    /**
     * 팀 삭제
     */
    fun deleteTeam(teamId: String) = launchWithUiState {
        if (repository.deleteTeam(teamId)) {
            _teams.value = _teams.value.filterNot { it.id == teamId }
        }
    }

    /**
     * 특정 프로그램 ID에 해당하는 미션 목록 로드
     */
    fun loadMissionsByProgramId(programId: String) = launchWithUiState {
        _missions.value = repository.getMissionsByProgramId(programId)
    }

    /**
     * 특정 프로그램 ID에 해당하는 팀 미션 진행 상황 로드
     */
    fun loadTeamProgressByProgramId(programId: String) = launchWithUiState {
        _teamProgress.value = repository.getTeamProgressByProgramId(programId)
    }

    // `ClassDetailModel`과 `TeamProgressListModel`에 대한 함수는
    // 해당 ViewModel을 사용하는 화면에서 직접 호출하거나,
    // 필요에 따라 ClassViewModel이 이들을 composition하여 데이터를 제공하도록 구성할 수 있습니다.
    // 현재 구조에서는 ClassViewModel이 ClassDetailModel과 TeamProgressListModel의 역할을
    // 직접 수행하는 대신, 그들과 협력하여 데이터를 제공하는 형태로 변경되었습니다.
    // 예를 들어, ClassDetailModel에 접근해야 한다면 해당 모델의 인스턴스를
    // ViewModel의 생성자를 통해 주입받아야 합니다.
    // fun loadClassDetail(classId: String) = classDetailModel.loadClassDetail(classId)
    // fun getProgressStatistics(): ProgressStatistics = teamProgressModel.getProgressStatistics()


    // --- 유틸리티 함수 ---

    /**
     * 로딩 및 에러 처리를 포함하는 코루틴 런치 헬퍼 함수
     */
    private fun launchWithUiState(block: suspend () -> Unit) = viewModelScope.launch {
        _uiState.value = UiState.Loading
        try {
            block()
            _uiState.value = UiState.Success
        } catch (e: Exception) {
            _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
        }
    }
}