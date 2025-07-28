package com.example.bogoargo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.model.Class
import com.example.bogoargo.data.model.Program
import com.example.bogoargo.data.model.Team
import com.example.bogoargo.data.model.User
import com.example.bogoargo.data.repository.ClassRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClassViewModel(
    private val repository: ClassRepository = ClassRepository()
) : ViewModel() {

    private val _classes = MutableStateFlow<List<Class>>(emptyList())
    val classes: StateFlow<List<Class>> = _classes.asStateFlow()

    private val _programs = MutableStateFlow<List<Program>>(emptyList())
    val programs: StateFlow<List<Program>> = _programs.asStateFlow()

    private val _members = MutableStateFlow<List<User>>(emptyList())
    val members: StateFlow<List<User>> = _members.asStateFlow()

    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadClasses()
    }

    fun loadClasses() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val classesFromServer = repository.getAllClasses()
                _classes.value = classesFromServer
            } catch (e: Exception) {
                _error.value = "반 목록을 불러오는데 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

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

    fun createClass(classData: Class) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val createdClass = repository.createClass(classData)
                // 새로 생성된 반을 목록에 추가
                _classes.value = _classes.value + createdClass
            } catch (e: Exception) {
                _error.value = "반 생성에 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 구성원 관련 메소드
    fun loadMembersByClassId(classId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val membersFromServer = repository.getMembersByClassId(classId)
                _members.value = membersFromServer
            } catch (e: Exception) {
                _error.value = "구성원 목록을 불러오는데 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 팀 관련 메소드
    fun loadTeamsByClassId(classId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val teamsFromServer = repository.getTeamsByClassId(classId)
                _teams.value = teamsFromServer
            } catch (e: Exception) {
                _error.value = "팀 목록을 불러오는데 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createTeam(team: Team) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val createdTeam = repository.createTeam(team)
                _teams.value = _teams.value + createdTeam
            } catch (e: Exception) {
                _error.value = "팀 생성에 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTeam(team: Team) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val updatedTeam = repository.updateTeam(team)
                _teams.value = _teams.value.map {
                    if (it.id == updatedTeam.id) updatedTeam else it
                }
            } catch (e: Exception) {
                _error.value = "팀 수정에 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTeam(teamId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = repository.deleteTeam(teamId)
                if (success) {
                    _teams.value = _teams.value.filter { it.id != teamId }
                }
            } catch (e: Exception) {
                _error.value = "팀 삭제에 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}