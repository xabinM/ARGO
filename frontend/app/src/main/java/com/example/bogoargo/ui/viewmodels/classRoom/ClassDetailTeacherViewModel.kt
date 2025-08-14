package com.example.bogoargo.ui.viewmodels.classRoom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.domain.model.Class
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.StudentInfo
import com.example.bogoargo.domain.model.TeamDetail
import com.example.bogoargo.domain.model.ClassStatistics
import com.example.bogoargo.domain.use_case.classroom.GetTeacherClassDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassDetailTeacherViewModel @Inject constructor(
    private val getTeacherClassDetailUseCase: GetTeacherClassDetailUseCase
) : ViewModel() {

    private val _classInfo = MutableStateFlow<Class?>(null)
    val classInfo: StateFlow<Class?> = _classInfo.asStateFlow()

    private val _students = MutableStateFlow<List<StudentInfo>>(emptyList())
    val students: StateFlow<List<StudentInfo>> = _students.asStateFlow()

    private val _teams = MutableStateFlow<List<TeamDetail>>(emptyList())
    val teams: StateFlow<List<TeamDetail>> = _teams.asStateFlow()

    private val _statistics = MutableStateFlow<ClassStatistics?>(null)
    val statistics: StateFlow<ClassStatistics?> = _statistics.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun getClassDetail(classId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            when (val result = getTeacherClassDetailUseCase(classId)) {
                is DataResult.Success -> {
                    result.data?.let { teacherClassDetail ->
                        _classInfo.value = teacherClassDetail.data
                        _students.value = teacherClassDetail.students ?: emptyList()
                        _teams.value = teacherClassDetail.teams ?: emptyList()
                        _statistics.value = teacherClassDetail.statistics
                    }
                }
                is DataResult.Error -> {
                    _errorMessage.value = result.exception.message ?: "알 수 없는 오류가 발생했습니다."
                }
                else -> Unit
            }

            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}