package com.example.bogoargo.ui.viewmodels.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.domain.model.Class
import com.example.bogoargo.domain.model.Team
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.use_case.classroom.GetClassDetailUseCase
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
    val memberCount: Int,
    val isMyTeam: Boolean = false
)

@HiltViewModel
class StudentClassDetailViewModel @Inject constructor(
    private val getClassDetailUseCase: GetClassDetailUseCase
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
        // 실제로는 백엔드에서 가져와야 하지만, 더미 데이터로 대체
        val dummyTeams = when (classId) {
            1L -> listOf(
                StudentTeamInfo(1L, "역사탐험대", 3, true),
                StudentTeamInfo(2L, "문화유산지킴이", 3),
                StudentTeamInfo(3L, "궁궐수호대", 3),
                StudentTeamInfo(4L, "전통문화사랑단", 3),
                StudentTeamInfo(5L, "한국사마스터", 3)
            )
            2L -> listOf(
                StudentTeamInfo(6L, "바다탐험대", 4, true),
                StudentTeamInfo(7L, "해양생물연구팀", 3),
                StudentTeamInfo(8L, "환경보호단", 4),
                StudentTeamInfo(9L, "푸른바다지킴이", 3),
                StudentTeamInfo(10L, "해운대탐험대", 3),
                StudentTeamInfo(11L, "물고기친구들", 3)
            )
            3L -> listOf(
                StudentTeamInfo(12L, "불국사탐험대", 4, true),
                StudentTeamInfo(13L, "석굴암수호대", 5),
                StudentTeamInfo(14L, "신라역사단", 4),
                StudentTeamInfo(15L, "경주문화지킴이", 5)
            )
            4L -> listOf(
                StudentTeamInfo(16L, "과학실험단", 4, true),
                StudentTeamInfo(17L, "미래과학자", 4),
                StudentTeamInfo(18L, "로봇친구들", 4)
            )
            5L -> listOf(
                StudentTeamInfo(19L, "숲속탐험대", 5, true),
                StudentTeamInfo(20L, "자연사랑단", 5),
                StudentTeamInfo(21L, "생태계지킴이", 5),
                StudentTeamInfo(22L, "동식물친구들", 5),
                StudentTeamInfo(23L, "지리산탐험대", 5)
            )
            else -> emptyList()
        }
        
        val myTeam = dummyTeams.find { it.isMyTeam }
        
        _uiState.value = _uiState.value.copy(
            myTeam = myTeam,
            allTeams = dummyTeams
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
                    createdAt = LocalDate.now().minusDays(14)
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
                    createdAt = LocalDate.now().minusDays(7)
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
                    createdAt = LocalDate.now().minusDays(10)
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
                    createdAt = LocalDate.now().minusDays(20)
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
                    createdAt = LocalDate.now().minusDays(3)
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
                    createdAt = LocalDate.now().minusDays(1)
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