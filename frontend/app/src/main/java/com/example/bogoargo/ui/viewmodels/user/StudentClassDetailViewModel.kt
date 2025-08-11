package com.example.bogoargo.ui.viewmodels.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.domain.model.*
import com.example.bogoargo.domain.use_case.classroom.GetStudentClassDetailUseCase
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
    val classDetail: StudentClassDetail? = null,
    val myTeam: TeamDetail? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class StudentClassDetailViewModel @Inject constructor(
    private val getStudentClassDetailUseCase: GetStudentClassDetailUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(StudentClassDetailUiState())
    val uiState: StateFlow<StudentClassDetailUiState> = _uiState.asStateFlow()

    fun loadClassDetail(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = getStudentClassDetailUseCase(classId)) {
                is DataResult.Success -> {
                    val currentUserId = preferencesManager.getUserId() ?: 1L
                    val myTeam = result.data.teams.find { team ->
                        team.members.any { it.studentId == currentUserId }
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        classDetail = result.data,
                        myTeam = myTeam,
                        errorMessage = null
                    )
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
    
    private fun createDummyStudentClassDetail(classId: Long): StudentClassDetail {
        val currentUserId = preferencesManager.getUserId() ?: 1L
        
        val (classInfo, students, teams) = when (classId) {
            1L -> createClass1DummyData()
            2L -> createClass2DummyData()
            3L -> createClass3DummyData()
            4L -> createClass4DummyData()
            5L -> createClass5DummyData()
            else -> createDefaultDummyData(classId)
        }
        
        return StudentClassDetail(
            classInfo = classInfo,
            students = students,
            teams = teams,
            statistics = ClassStatistics(
                totalStudents = students.size,
                totalTeams = teams.size
            )
        )
    }
    
    private fun createClass1DummyData(): Triple<ClassDetailInfo, List<StudentInfo>, List<TeamDetail>> {
        val classInfo = ClassDetailInfo(
            classId = 1L,
            className = "6학년 1반 역사탐험",
            description = "우리나라의 문화유산을 직접 체험하며 역사를 배워요",
            location = "서울 경복궁",
            activityDate = LocalDate.now().plusDays(7),
            maxStudents = 30,
            status = "active",
            inviteCode = null,
            teacherId = null,
            teacherName = "김선생님",
            createdAt = null
        )

        val students = listOf(
            StudentInfo(1L, "김민수", 1L, "역사탐험대", LocalDate.now().minusDays(14)),
            StudentInfo(2L, "박지영", 1L, "역사탐험대", LocalDate.now().minusDays(13)),
            StudentInfo(3L, "이서준", 1L, "역사탐험대", LocalDate.now().minusDays(12)),
            StudentInfo(4L, "최하늘", 2L, "문화유산지킴이", LocalDate.now().minusDays(11)),
            StudentInfo(5L, "정예린", 2L, "문화유산지킴이", LocalDate.now().minusDays(10)),
            StudentInfo(6L, "강도현", 2L, "문화유산지킴이", LocalDate.now().minusDays(9))
        )

        val teams = listOf(
            TeamDetail(
                teamId = 1L,
                teamName = "역사탐험대",
                memberCount = 3,
                totalScore = 150,
                members = listOf(
                    TeamMemberInfo(1L, "김민수"),
                    TeamMemberInfo(2L, "박지영"),
                    TeamMemberInfo(3L, "이서준")
                )
            ),
            TeamDetail(
                teamId = 2L,
                teamName = "문화유산지킴이",
                memberCount = 3,
                totalScore = 120,
                members = listOf(
                    TeamMemberInfo(4L, "최하늘"),
                    TeamMemberInfo(5L, "정예린"),
                    TeamMemberInfo(6L, "강도현")
                )
            )
        )

        return Triple(classInfo, students, teams)
    }

    private fun createClass2DummyData(): Triple<ClassDetailInfo, List<StudentInfo>, List<TeamDetail>> {
        val classInfo = ClassDetailInfo(
            classId = 2L,
            className = "5학년 특별활동반",
            description = "해양 생태계를 탐구하고 환경 보호의 중요성을 배워요",
            location = "부산 해운대",
            activityDate = LocalDate.now().plusDays(14),
            maxStudents = 25,
            status = "active",
            inviteCode = null,
            teacherId = null,
            teacherName = "이선생님",
            createdAt = null
        )

        val students = listOf(
            StudentInfo(1L, "김민수", 6L, "바다탐험대", LocalDate.now().minusDays(7)),
            StudentInfo(16L, "송유진", 6L, "바다탐험대", LocalDate.now().minusDays(6)),
            StudentInfo(17L, "장민호", 6L, "바다탐험대", LocalDate.now().minusDays(5)),
            StudentInfo(19L, "윤서연", 7L, "해양생물연구팀", LocalDate.now().minusDays(4))
        )

        val teams = listOf(
            TeamDetail(
                teamId = 6L,
                teamName = "바다탐험대",
                memberCount = 3,
                totalScore = 200,
                members = listOf(
                    TeamMemberInfo(1L, "김민수"),
                    TeamMemberInfo(16L, "송유진"),
                    TeamMemberInfo(17L, "장민호")
                )
            ),
            TeamDetail(
                teamId = 7L,
                teamName = "해양생물연구팀",
                memberCount = 1,
                totalScore = 80,
                members = listOf(
                    TeamMemberInfo(19L, "윤서연")
                )
            )
        )

        return Triple(classInfo, students, teams)
    }

    private fun createClass3DummyData(): Triple<ClassDetailInfo, List<StudentInfo>, List<TeamDetail>> {
        val classInfo = ClassDetailInfo(
            classId = 3L,
            className = "문화유산 탐방반",
            description = "신라의 천년 역사를 간직한 불국사와 석굴암 탐방",
            location = "경주 불국사",
            activityDate = LocalDate.now().plusDays(21),
            maxStudents = 20,
            status = "active",
            inviteCode = null,
            teacherId = null,
            teacherName = "박선생님",
            createdAt = null
        )

        val students = listOf(
            StudentInfo(1L, "김민수", 12L, "불국사탐험대", LocalDate.now().minusDays(10)),
            StudentInfo(35L, "한지우", 12L, "불국사탐험대", LocalDate.now().minusDays(9)),
            StudentInfo(36L, "오태민", 12L, "불국사탐험대", LocalDate.now().minusDays(8))
        )

        val teams = listOf(
            TeamDetail(
                teamId = 12L,
                teamName = "불국사탐험대",
                memberCount = 3,
                totalScore = 180,
                members = listOf(
                    TeamMemberInfo(1L, "김민수"),
                    TeamMemberInfo(35L, "한지우"),
                    TeamMemberInfo(36L, "오태민")
                )
            )
        )

        return Triple(classInfo, students, teams)
    }

    private fun createClass4DummyData(): Triple<ClassDetailInfo, List<StudentInfo>, List<TeamDetail>> {
        val classInfo = ClassDetailInfo(
            classId = 4L,
            className = "과학탐구반",
            description = "최신 과학 기술을 체험하고 미래 과학자의 꿈을 키워요",
            location = "대전 국립과학관",
            activityDate = LocalDate.now().plusDays(5),
            maxStudents = 30,
            status = "active",
            inviteCode = null,
            teacherId = null,
            teacherName = "최선생님",
            createdAt = null
        )

        val students = listOf(
            StudentInfo(1L, "김민수", 16L, "과학실험단", LocalDate.now().minusDays(20)),
            StudentInfo(52L, "신동현", 16L, "과학실험단", LocalDate.now().minusDays(19)),
            StudentInfo(53L, "배수민", 16L, "과학실험단", LocalDate.now().minusDays(18))
        )

        val teams = listOf(
            TeamDetail(
                teamId = 16L,
                teamName = "과학실험단",
                memberCount = 3,
                totalScore = 220,
                members = listOf(
                    TeamMemberInfo(1L, "김민수"),
                    TeamMemberInfo(52L, "신동현"),
                    TeamMemberInfo(53L, "배수민")
                )
            )
        )

        return Triple(classInfo, students, teams)
    }

    private fun createClass5DummyData(): Triple<ClassDetailInfo, List<StudentInfo>, List<TeamDetail>> {
        val classInfo = ClassDetailInfo(
            classId = 5L,
            className = "자연생태 체험반",
            description = "숲 속 생태계를 관찰하고 자연의 소중함을 배워요",
            location = "지리산 국립공원",
            activityDate = LocalDate.now().plusDays(30),
            maxStudents = 25,
            status = "active",
            inviteCode = null,
            teacherId = null,
            teacherName = "정선생님",
            createdAt = null
        )

        val students = listOf(
            StudentInfo(1L, "김민수", 19L, "숲속탐험대", LocalDate.now().minusDays(3)),
            StudentInfo(63L, "임채원", 19L, "숲속탐험대", LocalDate.now().minusDays(2)),
            StudentInfo(64L, "조현우", 19L, "숲속탐험대", LocalDate.now().minusDays(1))
        )

        val teams = listOf(
            TeamDetail(
                teamId = 19L,
                teamName = "숲속탐험대",
                memberCount = 3,
                totalScore = 95,
                members = listOf(
                    TeamMemberInfo(1L, "김민수"),
                    TeamMemberInfo(63L, "임채원"),
                    TeamMemberInfo(64L, "조현우")
                )
            )
        )

        return Triple(classInfo, students, teams)
    }

    private fun createDefaultDummyData(classId: Long): Triple<ClassDetailInfo, List<StudentInfo>, List<TeamDetail>> {
        val classInfo = ClassDetailInfo(
            classId = classId,
            className = "테스트 반",
            description = "개발용 테스트 반입니다",
            location = "테스트 장소",
            activityDate = LocalDate.now().plusDays(1),
            maxStudents = 20,
            status = "active",
            inviteCode = null,
            teacherId = null,
            teacherName = "테스트선생님",
            createdAt = null
        )

        val students = listOf(
            StudentInfo(1L, "김민수", 1L, "테스트팀", LocalDate.now().minusDays(1))
        )

        val teams = listOf(
            TeamDetail(
                teamId = 1L,
                teamName = "테스트팀",
                memberCount = 1,
                totalScore = 50,
                members = listOf(
                    TeamMemberInfo(1L, "김민수")
                )
            )
        )

        return Triple(classInfo, students, teams)
    }

    fun loadDummyData(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 짧은 딜레이로 로딩 시뮬레이션
            kotlinx.coroutines.delay(500)

            // 더미 반 상세 데이터 생성
            val dummyClassDetail = createDummyStudentClassDetail(classId)

            // 현재 사용자가 속한 팀 찾기
            val currentUserId = preferencesManager.getUserId() ?: 1L
            val myTeam = dummyClassDetail.teams.find { team ->
                team.members.any { it.studentId == currentUserId }
            }
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                classDetail = dummyClassDetail,
                myTeam = myTeam,
                errorMessage = null
            )
        }
    }
}