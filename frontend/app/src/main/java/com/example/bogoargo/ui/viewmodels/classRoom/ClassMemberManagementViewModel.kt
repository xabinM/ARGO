package com.example.bogoargo.ui.viewmodels.classRoom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.dto.response.UserDataDto
import com.example.bogoargo.data.dto.response.StudentListResponseDto
import com.example.bogoargo.domain.model.Application
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.use_case.classroom.ApproveApplicationUseCase
import com.example.bogoargo.domain.use_case.classroom.GetApplicationListUseCase
import com.example.bogoargo.domain.use_case.classroom.GetClassMemberListUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class ClassMemberManagementUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val actionSuccess: Boolean = false,
    val actionMessage: String? = null,
    val pendingApplications: List<Application> = emptyList(),
    val classMembers: List<StudentListResponseDto> = emptyList(),
    val selectedApplicationIds: Set<Long> = emptySet(),
    val currentTab: ManagementTab = ManagementTab.MEMBERS
)

enum class ManagementTab {
    MEMBERS,      // 반 구성원 관리
    APPLICATIONS  // 참여신청 관리
}

@HiltViewModel
class ClassMemberManagementViewModel @Inject constructor(
    private val getApplicationListUseCase: GetApplicationListUseCase,
    private val approveApplicationUseCase: ApproveApplicationUseCase,
    private val getClassMemberListUseCase: GetClassMemberListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassMemberManagementUiState())
    val uiState: StateFlow<ClassMemberManagementUiState> = _uiState

    fun switchTab(tab: ManagementTab, classId: Long) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
        loadData(classId) // 탭 변경 시 자동 데이터 로드
    }


    fun loadData(classId: Long) {
        when (_uiState.value.currentTab) {
            ManagementTab.MEMBERS -> loadClassMembers(classId)
            ManagementTab.APPLICATIONS -> loadPendingApplications(classId)
        }
    }

    fun loadClassMembers(classId: Long, status: String = "assigned", page: Int = 1, size: Int = 50) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = getClassMemberListUseCase(classId, status, page, size)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        classMembers = result.data.data.students,
                        errorMessage = null
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message ?: "Failed to load class members"
                    )
                }
                else -> Unit
            }
        }
    }

    fun loadPendingApplications(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = getApplicationListUseCase(classId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pendingApplications = result.data,
                        errorMessage = null
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message ?: "Failed to load applications"
                    )
                }
                else -> Unit
            }
        }
    }

    fun selectApplication(applicationId: Long) {
        val currentSelected = _uiState.value.selectedApplicationIds
        _uiState.value = _uiState.value.copy(
            selectedApplicationIds = if (currentSelected.contains(applicationId)) {
                currentSelected - applicationId
            } else {
                currentSelected + applicationId
            }
        )
    }

    fun selectAllApplications() {
        val allIds = _uiState.value.pendingApplications.map { it.applicationId }.toSet()
        _uiState.value = _uiState.value.copy(selectedApplicationIds = allIds)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedApplicationIds = emptySet())
    }

    fun approveSelectedApplications(classId: Long) {
        val selectedIds = _uiState.value.selectedApplicationIds.toList()
        if (selectedIds.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "No applications selected")
            return
        }
        
        processApplications(classId, "approve", selectedIds)
    }

    fun rejectSelectedApplications(classId: Long) {
        val selectedIds = _uiState.value.selectedApplicationIds.toList()
        if (selectedIds.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "No applications selected")
            return
        }
        
        processApplications(classId, "reject", selectedIds)
    }

    fun approveSingleApplication(classId: Long, applicationId: Long) {
        processApplications(classId, "approve", listOf(applicationId))
    }

    fun rejectSingleApplication(classId: Long, applicationId: Long) {
        processApplications(classId, "reject", listOf(applicationId))
    }

    private fun processApplications(classId: Long, action: String, applicationIds: List<Long>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, actionSuccess = false)
            
            when (val result = approveApplicationUseCase(classId, action, applicationIds)) {
                is DataResult.Success -> {
                    val actionText = if (action.uppercase() == "APPROVE") "approved" else "rejected"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actionSuccess = true,
                        actionMessage = "${applicationIds.size} application(s) $actionText successfully",
                        selectedApplicationIds = emptySet()
                    )
                    // Reload the list to reflect changes
                    loadPendingApplications(classId)
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message ?: "Failed to process applications"
                    )
                }
                else -> Unit
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            actionMessage = null,
            actionSuccess = false
        )
    }

    fun clearState() {
        _uiState.value = ClassMemberManagementUiState()
    }
}