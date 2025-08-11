package com.example.bogoargo.ui.viewmodels.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.domain.model.*
import com.example.bogoargo.domain.use_case.classroom.GetStudentClassDetailUseCase
import com.example.bogoargo.data.storage.SecureStorage
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
    private val secureStorage: SecureStorage
) : ViewModel() {
    private val _uiState = MutableStateFlow(StudentClassDetailUiState())
    val uiState: StateFlow<StudentClassDetailUiState> = _uiState.asStateFlow()

    fun loadClassDetail(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = getStudentClassDetailUseCase(classId)) {
                is DataResult.Success -> {
                    val currentUser = secureStorage.getUser()
                    val currentUserId = currentUser?.userId ?: 1L
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
    
}