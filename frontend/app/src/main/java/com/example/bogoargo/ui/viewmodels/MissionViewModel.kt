package com.example.bogoargo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.Mission
import com.example.bogoargo.domain.model.MissionSpot
import com.example.bogoargo.domain.use_case.mission.GetMissionSpotsUseCase
import com.example.bogoargo.domain.use_case.mission.GetMissionUseCase
import com.example.bogoargo.domain.use_case.mission.ManageMissionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MissionViewModel @Inject constructor(
    private val getMissionSpotsUseCase: GetMissionSpotsUseCase,
    private val getMissionUseCase: GetMissionUseCase,
    private val manageMissionUseCase: ManageMissionUseCase
) : ViewModel() {

    data class MissionUiState(
        val isLoading: Boolean = false,
        val missions: List<Mission> = emptyList(),
        val missionSpots: List<MissionSpot> = emptyList(),
        val currentMission: Mission? = null,
        val errorMessage: String? = null,
        val missionProgress: Int = 0
    )

    private val _uiState = MutableStateFlow(MissionUiState())
    val uiState: StateFlow<MissionUiState> = _uiState.asStateFlow()

    fun loadMissions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = getMissionUseCase.getMissions()) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        missions = result.data
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // Loading state already set
                }
            }
        }
    }
    
    fun loadMissionSpots(classId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = getMissionSpotsUseCase(classId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        missionSpots = result.data
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // Loading state already set
                }
            }
        }
    }
    
    fun loadMissionById(missionId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = getMissionUseCase.getMissionById(missionId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentMission = result.data
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // Loading state already set
                }
            }
        }
    }
    
    fun startMission(missionId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = manageMissionUseCase.startMission(missionId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    // Refresh mission data after starting
                    loadMissionById(missionId)
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // Loading state already set
                }
            }
        }
    }
    
    fun completeMission(missionId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = manageMissionUseCase.completeMission(missionId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    // Refresh mission data after completion
                    loadMissionById(missionId)
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // Loading state already set
                }
            }
        }
    }
    
    fun loadMissionProgress(missionId: Long) {
        viewModelScope.launch {
            when (val result = getMissionUseCase.getMissionProgress(missionId)) {
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(missionProgress = result.data)
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // No loading state for progress as it's a background operation
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearMissionSpots() {
        _uiState.value = _uiState.value.copy(missionSpots = emptyList())
    }
}