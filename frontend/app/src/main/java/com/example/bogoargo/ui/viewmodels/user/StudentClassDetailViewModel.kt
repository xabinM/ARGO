package com.example.bogoargo.ui.viewmodels.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.domain.model.Class
import com.example.bogoargo.domain.model.Team
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.use_case.classroom.GetClassDetailUseCase
import com.example.bogoargo.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

data class StudentClassDetailUiState(
    val isLoading: Boolean = false,
    val classDetail: Class? = null,
    val myTeam: StudentTeamInfo? = null,
    val allTeams: List<StudentTeamInfo> = emptyList(),
    val errorMessage: String? = null
)

data class StudentTeamInfo(
    val teamId: Long,
    val teamName: String,
    val memberIds: List<Long>, // 팀원 userId 리스트
    val memberCount: Int,
    val isMyTeam: Boolean = false
)

@HiltViewModel
class StudentClassDetailViewModel @Inject constructor(
    private val getClassDetailUseCase: GetClassDetailUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(StudentClassDetailUiState())
    val uiState: StateFlow<StudentClassDetailUiState> = _uiState.asStateFlow()

    fun loadClassDetail(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = getClassDetailUseCase(classId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        classDetail = result.data,
                        errorMessage = null
                    )
                    
                    // 더미 팀 데이터 로드
                    loadDummyTeamData(classId)
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "반 정보를 불러올 수 없습니다."
                    )
                }
                is DataResult.Loading -> {
                    // 이미 로딩 상태로 설정됨
                }
            }
        }
    }
    
    private fun loadDummyTeamData(classId: Long) {
        // 현재 로그인한 사용자의 ID 가져오기
        val currentUserId = preferencesManager.getUserId() ?: 1L // 기본값 1L (더미 데이터용)
        
        // 실제로는 백엔드에서 가져와야 하지만, 더미 데이터로 대체
        // 각 팀에 멤버 ID 리스트 추가
        val dummyTeams = when (classId) {
            1L -> listOf(
                StudentTeamInfo(1L, "역사탐험대", listOf(1L, 2L, 3L), 3),
                StudentTeamInfo(2L, "문화유산지킴이", listOf(4L, 5L, 6L), 3),
                StudentTeamInfo(3L, "궁궐수호대", listOf(7L, 8L, 9L), 3),
                StudentTeamInfo(4L, "전통문화사랑단", listOf(10L, 11L, 12L), 3),
                StudentTeamInfo(5L, "한국사마스터", listOf(13L, 14L, 15L), 3)
            )
            2L -> listOf(
                StudentTeamInfo(6L, "바다탐험대", listOf(1L, 16L, 17L, 18L), 4),
                StudentTeamInfo(7L, "해양생물연구팀", listOf(19L, 20L, 21L), 3),
                StudentTeamInfo(8L, "환경보호단", listOf(22L, 23L, 24L, 25L), 4),
                StudentTeamInfo(9L, "푸른바다지킴이", listOf(26L, 27L, 28L), 3),
                StudentTeamInfo(10L, "해운대탐험대", listOf(29L, 30L, 31L), 3),
                StudentTeamInfo(11L, "물고기친구들", listOf(32L, 33L, 34L), 3)
            )
            3L -> listOf(
                StudentTeamInfo(12L, "불국사탐험대", listOf(1L, 35L, 36L, 37L), 4),
                StudentTeamInfo(13L, "석굴암수호대", listOf(38L, 39L, 40L, 41L, 42L), 5),
                StudentTeamInfo(14L, "신라역사단", listOf(43L, 44L, 45L, 46L), 4),
                StudentTeamInfo(15L, "경주문화지킴이", listOf(47L, 48L, 49L, 50L, 51L), 5)
            )
            4L -> listOf(
                StudentTeamInfo(16L, "과학실험단", listOf(1L, 52L, 53L, 54L), 4),
                StudentTeamInfo(17L, "미래과학자", listOf(55L, 56L, 57L, 58L), 4),
                StudentTeamInfo(18L, "로봇친구들", listOf(59L, 60L, 61L, 62L), 4)
            )
            5L -> listOf(
                StudentTeamInfo(19L, "숲속탐험대", listOf(1L, 63L, 64L, 65L, 66L), 5),
                StudentTeamInfo(20L, "자연사랑단", listOf(67L, 68L, 69L, 70L, 71L), 5),
                StudentTeamInfo(21L, "생태계지킴이", listOf(72L, 73L, 74L, 75L, 76L), 5),
                StudentTeamInfo(22L, "동식물친구들", listOf(77L, 78L, 79L, 80L, 81L), 5),
                StudentTeamInfo(23L, "지리산탐험대", listOf(82L, 83L, 84L, 85L, 86L), 5)
            )
            else -> emptyList()
        }
        
        // 현재 사용자가 속한 팀 찾기
        val teamsWithMyFlag = dummyTeams.map { team ->
            team.copy(isMyTeam = team.memberIds.contains(currentUserId))
        }
        
        val myTeam = teamsWithMyFlag.find { it.isMyTeam }
        
        _uiState.value = _uiState.value.copy(
            myTeam = myTeam,
            allTeams = teamsWithMyFlag
        )
    }
    
    fun loadDummyData(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // 짧은 딜레이로 로딩 시뮬레이션
            kotlinx.coroutines.delay(500)
            
            // 더미 반 데이터 생성
            val dummyClass = when (classId) {
                1L -> Class(
                    classId = 1L,
                    className = "6학년 1반 역사탐험",
                    description = "우리나라의 문화유산을 직접 체험하며 역사를 배워요",
                    location = "서울 경복궁",
                    activityDate = LocalDate.now().plusDays(7),
                    currentStudents = 15,
                    maxStudents = 30,
                    studentCount = 15,
                    teamCount = 5,
                    status = Class.ClassStatus.ACTIVE,
                    inviteCode = "HIST2024",
                    createdAt = LocalDate.now().minusDays(14),
                    isFull = false
                )
                2L -> Class(
                    classId = 2L,
                    className = "5학년 특별활동반",
                    description = "해양 생태계를 탐구하고 환경 보호의 중요성을 배워요",
                    location = "부산 해운대",
                    activityDate = LocalDate.now().plusDays(14),
                    currentStudents = 20,
                    maxStudents = 25,
                    studentCount = 20,
                    teamCount = 6,
                    status = Class.ClassStatus.ACTIVE,
                    inviteCode = "OCEAN2024",
                    createdAt = LocalDate.now().minusDays(7),
                    isFull = false
                )
                3L -> Class(
                    classId = 3L,
                    className = "문화유산 탐방반",
                    description = "신라의 천년 역사를 간직한 불국사와 석굴암 탐방",
                    location = "경주 불국사",
                    activityDate = LocalDate.now().plusDays(21),
                    currentStudents = 18,
                    maxStudents = 20,
                    studentCount = 18,
                    teamCount = 4,
                    status = Class.ClassStatus.ACTIVE,
                    inviteCode = "TEMPLE2024",
                    createdAt = LocalDate.now().minusDays(10),
                    isFull = false
                )
                4L -> Class(
                    classId = 4L,
                    className = "과학탐구반",
                    description = "최신 과학 기술을 체험하고 미래 과학자의 꿈을 키워요",
                    location = "대전 국립과학관",
                    activityDate = LocalDate.now().plusDays(5),
                    currentStudents = 12,
                    maxStudents = 30,
                    studentCount = 12,
                    teamCount = 3,
                    status = Class.ClassStatus.ACTIVE,
                    inviteCode = "SCI2024",
                    createdAt = LocalDate.now().minusDays(20),
                    isFull = false
                )
                5L -> Class(
                    classId = 5L,
                    className = "자연생태 체험반",
                    description = "숲 속 생태계를 관찰하고 자연의 소중함을 배워요",
                    location = "지리산 국립공원",
                    activityDate = LocalDate.now().plusDays(30),
                    currentStudents = 25,
                    maxStudents = 25,
                    studentCount = 25,
                    teamCount = 5,
                    status = Class.ClassStatus.ACTIVE,
                    inviteCode = "NATURE2024",
                    createdAt = LocalDate.now().minusDays(3),
                    isFull = true
                )
                else -> Class(
                    classId = classId,
                    className = "테스트 반",
                    description = "개발용 테스트 반입니다",
                    location = "테스트 장소",
                    activityDate = LocalDate.now().plusDays(1),
                    currentStudents = 10,
                    maxStudents = 20,
                    studentCount = 10,
                    teamCount = 2,
                    status = Class.ClassStatus.ACTIVE,
                    inviteCode = "TEST2024",
                    createdAt = LocalDate.now().minusDays(1),
                    isFull = false
                )
            }
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                classDetail = dummyClass,
                errorMessage = null
            )
            
            // 팀 데이터도 로드
            loadDummyTeamData(classId)
        }
    }
}