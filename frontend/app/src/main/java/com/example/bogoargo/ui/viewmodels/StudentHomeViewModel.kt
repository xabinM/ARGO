package com.example.bogoargo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.domain.model.Class
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.use_case.classroom.GetStudentClassListUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val welcomeMessage: String = "Welcome to BogoArgo",
    val items: List<String> = emptyList(),
    val selectedItemIndex: Int? = null,
    val classes: List<Class> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class StudentHomeViewModel @Inject constructor(
    private val getStudentClassListUseCase: GetStudentClassListUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadItems()
        loadClasses()
    }

    private fun loadItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Simulate loading data
            kotlinx.coroutines.delay(1000)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                items = listOf(
                    "Item 1",
                    "Item 2", 
                    "Item 3",
                    "Item 4",
                    "Item 5"
                )
            )
        }
    }

    private fun loadClasses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = getStudentClassListUseCase(page = 1, size = 10, status = "active")) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        classes = result.data,
                        errorMessage = null
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "반 목록을 불러올 수 없습니다."
                    )
                }
                is DataResult.Loading -> {
                    // 이미 위에서 isLoading = true로 설정했으므로 추가 작업 불필요
                }
            }
        }
    }

    fun selectItem(index: Int) {
        _uiState.value = _uiState.value.copy(selectedItemIndex = index)
    }

    fun refreshData() {
        loadItems()
        loadClasses()
    }
    
    fun loadDummyClasses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // 짧은 딜레이로 로딩 시뮬레이션
            kotlinx.coroutines.delay(500)
            
            val dummyClasses = listOf(
                Class(
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
                ),
                Class(
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
                ),
                Class(
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
                ),
                Class(
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
                ),
                Class(
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
            )
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                classes = dummyClasses,
                errorMessage = null
            )
        }
    }
}