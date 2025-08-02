package com.example.bogoargo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.model.Program
import com.example.bogoargo.data.model.ProgramStatus
import com.example.bogoargo.data.repository.ProgramRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProgramViewModel(
    private val repository: ProgramRepository = ProgramRepository()
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _programs = MutableStateFlow<List<Program>>(emptyList())
    val programs: StateFlow<List<Program>> = _programs.asStateFlow()

    private val _selectedProgram = MutableStateFlow<Program?>(null)
    val selectedProgram: StateFlow<Program?> = _selectedProgram.asStateFlow()

    private val _participants = MutableStateFlow<List<String>>(emptyList())
    val participants: StateFlow<List<String>> = _participants.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadProgramsByClassId(classId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val programsFromServer = repository.getProgramsByClassId(classId)
                _programs.value = programsFromServer
            } catch (e: Exception) {
                _error.value = "프로그램 목록을 불러오는데 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createProgram(
        classId: String,
        title: String,
        description: String,
        location: String,
        date: String,
        startTime: String,
        endTime: String,
        maxParticipants: Int
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            try {
                val programData = Program(
                    classId = classId,
                    title = title,
                    description = description,
                    location = location,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    maxParticipants = maxParticipants,
                    status = ProgramStatus.UPCOMING
                )
                val createdProgram = repository.createProgram(programData)
                _programs.value = _programs.value + createdProgram
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error("프로그램 생성에 실패했습니다: ${e.message}")
            }
        }
    }

    fun updateProgram(program: Program) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val updatedProgram = repository.updateProgram(program)
                _programs.value = _programs.value.map {
                    if (it.id == updatedProgram.id) updatedProgram else it
                }
                if (_selectedProgram.value?.id == updatedProgram.id) {
                    _selectedProgram.value = updatedProgram
                }
            } catch (e: Exception) {
                _error.value = "프로그램 수정에 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProgram(programId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = repository.deleteProgram(programId)
                if (success) {
                    _programs.value = _programs.value.filter { it.id != programId }
                    if (_selectedProgram.value?.id == programId) {
                        _selectedProgram.value = null
                    }
                }
            } catch (e: Exception) {
                _error.value = "프로그램 삭제에 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadProgramDetail(programId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val program = repository.getProgramById(programId)
                _selectedProgram.value = program
            } catch (e: Exception) {
                _error.value = "프로그램 상세 정보를 불러오는데 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProgramStatus(programId: String, status: ProgramStatus) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val updatedProgram = repository.updateProgramStatus(programId, status)
                updatedProgram?.let { program ->
                    _programs.value = _programs.value.map {
                        if (it.id == programId) program else it
                    }
                    if (_selectedProgram.value?.id == programId) {
                        _selectedProgram.value = program
                    }
                }
            } catch (e: Exception) {
                _error.value = "프로그램 상태 변경에 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun joinProgram(programId: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = repository.joinProgram(programId, userId)
                if (success) {
                    // 참여자 수 업데이트
                    _programs.value = _programs.value.map { program ->
                        if (program.id == programId) {
                            program.copy(participants = program.participants + 1)
                        } else program
                    }
                    
                    _selectedProgram.value?.let { program ->
                        if (program.id == programId) {
                            _selectedProgram.value = program.copy(participants = program.participants + 1)
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "프로그램 참여에 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun leaveProgram(programId: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = repository.leaveProgram(programId, userId)
                if (success) {
                    // 참여자 수 업데이트
                    _programs.value = _programs.value.map { program ->
                        if (program.id == programId) {
                            program.copy(participants = maxOf(0, program.participants - 1))
                        } else program
                    }
                    
                    _selectedProgram.value?.let { program ->
                        if (program.id == programId) {
                            _selectedProgram.value = program.copy(participants = maxOf(0, program.participants - 1))
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "프로그램 참여 취소에 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadProgramParticipants(programId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val participantsFromServer = repository.getProgramParticipants(programId)
                _participants.value = participantsFromServer
            } catch (e: Exception) {
                _error.value = "참여자 목록을 불러오는데 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    fun clearSelectedProgram() {
        _selectedProgram.value = null
    }
}