package com.example.bogoargo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.domain.model.Class
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.use_case.classroom.GetStudentClassListUseCase
import com.example.bogoargo.domain.use_case.classroom.ApplyClassUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val welcomeMessage: String = "🎒 와! 오늘은 신나는 현장체험학습 날이야!🌟",
    val classes: List<Class> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class StudentHomeViewModel @Inject constructor(
    private val getStudentClassListUseCase: GetStudentClassListUseCase,
    private val applyClassUseCase: ApplyClassUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadClasses()
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


    fun refreshData() {
        loadClasses()
    }
    
    fun applyToClass(inviteCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = applyClassUseCase(inviteCode)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                    // 반 신청 성공 후 반 목록을 다시 불러옴
                    loadClasses()
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message ?: "반 신청에 실패했습니다."
                    )
                }
                is DataResult.Loading -> {
                    // 이미 위에서 isLoading = true로 설정했으므로 추가 작업 불필요
                }
            }
        }
    }
    
}