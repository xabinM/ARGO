package com.example.bogoargo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.dto.ClassCreateRequest
import com.example.bogoargo.data.dto.response.ApplicationResponseDto
import com.example.bogoargo.data.dto.response.MessageResponseDto
import com.example.bogoargo.data.dto.response.UserDataDto
import com.example.bogoargo.data.dto.response.applyClassResponse
import com.example.bogoargo.data.model.Class
import com.example.bogoargo.data.repository.ClassRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ClassViewModel @Inject constructor(
    private val classRepository: ClassRepository
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _teacherClasses = MutableStateFlow<List<Class>>(emptyList())
    val teacherClasses: StateFlow<List<Class>> = _teacherClasses.asStateFlow()

    private val _studentClasses = MutableStateFlow<List<Class>>(emptyList())
    val studentClasses: StateFlow<List<Class>> = _studentClasses.asStateFlow()

    private val _classDetail = MutableStateFlow<Class?>(null)
    val classDetail: StateFlow<Class?> = _classDetail.asStateFlow()

    private val _applicationList = MutableStateFlow<ApplicationResponseDto?>(null)
    val applicationList: StateFlow<ApplicationResponseDto?> = _applicationList.asStateFlow()

    private val _classMemberList = MutableStateFlow<List<UserDataDto>>(emptyList())
    val classMemberList: StateFlow<List<UserDataDto>> = _classMemberList.asStateFlow()

    fun clearUiState() {
        _uiState.value = UiState.Idle
    }

    fun loadTeacherClassList(page: Int = 1, size: Int = 10, status: String? = "active") {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = classRepository.getTeacherClassList(page, size, status)
                result.fold(
                    onSuccess = { classes ->
                        _teacherClasses.value = classes
                        _uiState.value = UiState.Success
                    },
                    onFailure = { exception ->
                        _uiState.value = UiState.Error(exception.message ?: "교사 반 목록 로드 실패")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun loadStudentClassList(page: Int = 1, size: Int = 10, status: String? = "active") {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = classRepository.getStudentClassList(page, size, status)
                result.fold(
                    onSuccess = { classes ->
                        _studentClasses.value = classes
                        _uiState.value = UiState.Success
                    },
                    onFailure = { exception ->
                        _uiState.value = UiState.Error(exception.message ?: "학생 반 목록 로드 실패")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun loadClassDetail(classId: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = classRepository.getClassDetail(classId)
                result.fold(
                    onSuccess = { classData ->
                        _classDetail.value = classData
                        _uiState.value = UiState.Success
                    },
                    onFailure = { exception ->
                        _uiState.value = UiState.Error(exception.message ?: "반 상세 정보 로드 실패")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun createClass(
        year: Int,
        schoolName: String,
        className: String,
        description: String,
        region: String,
        maxStudents: Int
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val request = ClassCreateRequest(
                    year = year,
                    schoolName = schoolName,
                    className = className,
                    description = description,
                    region = region,
                    maxStudents = maxStudents
                )
                val result = classRepository.createClass(request)
                result.fold(
                    onSuccess = { createdClass ->
                        if (createdClass != null) {
                            _teacherClasses.value = _teacherClasses.value + createdClass
                        }
                        _uiState.value = UiState.Success
                    },
                    onFailure = { exception ->
                        _uiState.value = UiState.Error(exception.message ?: "반 생성 실패")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun loadApplicationList(classId: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = classRepository.getApplicationList(classId)
                result.fold(
                    onSuccess = { applicationResponse ->
                        _applicationList.value = applicationResponse
                        _uiState.value = UiState.Success
                    },
                    onFailure = { exception ->
                        _uiState.value = UiState.Error(exception.message ?: "신청 목록 로드 실패")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun approveApplication(classId: Long, applicationId: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = classRepository.approveApplication(classId, applicationId)
                result.fold(
                    onSuccess = { messageResponse ->
                        _uiState.value = UiState.Success
                        loadApplicationList(classId)
                    },
                    onFailure = { exception ->
                        _uiState.value = UiState.Error(exception.message ?: "신청 승인 실패")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun loadClassMemberList(classId: Long, status: String, page: Int = 10, size: Int = 10) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = classRepository.getClassMemberList(classId, status, page, size)
                result.fold(
                    onSuccess = { memberList ->
                        _classMemberList.value = memberList ?: emptyList()
                        _uiState.value = UiState.Success
                    },
                    onFailure = { exception ->
                        _uiState.value = UiState.Error(exception.message ?: "멤버 목록 로드 실패")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun deleteClass(classId: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = classRepository.deleteClass(classId)
                result.fold(
                    onSuccess = { messageResponse ->
                        _teacherClasses.value = _teacherClasses.value.filter { it.id != classId.toString() }
                        _uiState.value = UiState.Success
                    },
                    onFailure = { exception ->
                        _uiState.value = UiState.Error(exception.message ?: "반 삭제 실패")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun applyClass(inviteCode: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = classRepository.applyClass(inviteCode)
                result.fold(
                    onSuccess = { applyResponse ->
                        _uiState.value = UiState.Success
                        loadStudentClassList()
                    },
                    onFailure = { exception ->
                        _uiState.value = UiState.Error(exception.message ?: "반 참여 신청 실패")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun leaveClass(classId: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = classRepository.leaveClass(classId)
                result.fold(
                    onSuccess = { leaveResponse ->
                        _studentClasses.value = _studentClasses.value.filter { it.id != classId.toString() }
                        _uiState.value = UiState.Success
                    },
                    onFailure = { exception ->
                        _uiState.value = UiState.Error(exception.message ?: "반 탈퇴 실패")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }
}