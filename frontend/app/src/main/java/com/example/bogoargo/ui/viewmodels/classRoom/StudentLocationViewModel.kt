package com.example.bogoargo.ui.viewmodels.classRoom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.domain.model.StudentLocation
import com.example.bogoargo.domain.model.UserCoordinates
import com.example.bogoargo.domain.use_case.location.GetStudentLocationsByClassUseCase
import com.example.bogoargo.domain.use_case.location.GetCoordinatesByClassUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentLocationUiState(
    val isLoading: Boolean = false,
    val students: List<StudentLocation> = emptyList(),
    val userCoordinates: List<UserCoordinates> = emptyList(),
    val className: String = "",
    val classId: Long = 0L,
    val errorMessage: String? = null
)

@HiltViewModel
class StudentLocationViewModel @Inject constructor(
    private val getStudentLocationsByClassUseCase: GetStudentLocationsByClassUseCase,
    private val getCoordinatesByClassUseCase: GetCoordinatesByClassUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentLocationUiState())
    val uiState: StateFlow<StudentLocationUiState> = _uiState.asStateFlow()

    fun loadStudentLocations(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                getStudentLocationsByClassUseCase(classId).fold(
                    onSuccess = { studentLocationData ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            students = studentLocationData.students,
                            className = studentLocationData.className,
                            classId = studentLocationData.classId,
                            errorMessage = null
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "학생 위치 정보를 불러오는데 실패했습니다."
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다."
                )
            }
        }
    }

    fun loadCoordinatesByClass(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                getCoordinatesByClassUseCase(classId).fold(
                    onSuccess = { userCoordinates ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            userCoordinates = userCoordinates,
                            classId = classId,
                            errorMessage = null
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "좌표 정보를 불러오는데 실패했습니다."
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다."
                )
            }
        }
    }

    fun refreshStudentLocations() {
        val currentState = _uiState.value
        if (currentState.classId > 0L) {
            loadStudentLocations(currentState.classId)
        }
    }

    fun refreshCoordinates() {
        val currentState = _uiState.value
        if (currentState.classId > 0L) {
            loadCoordinatesByClass(currentState.classId)
        }
    }
}