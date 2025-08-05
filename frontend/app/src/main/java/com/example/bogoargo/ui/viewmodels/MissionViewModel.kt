package com.example.bogoargo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.domain.model.MissionSpot
import com.example.bogoargo.data.repository.MissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MissionViewModel @Inject constructor(
    private val missionRepository: MissionRepository
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _missionSpots = MutableStateFlow<List<MissionSpot>>(emptyList())
    val missionSpots: StateFlow<List<MissionSpot>> = _missionSpots.asStateFlow()

    fun clearUiState() {
        _uiState.value = UiState.Idle
    }

    fun loadMissionSpots(classId: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = missionRepository.getMissionSpots(classId)
                result.fold(
                    onSuccess = { spots ->
                        _missionSpots.value = spots
                        _uiState.value = UiState.Success
                    },
                    onFailure = { exception ->
                        _uiState.value = UiState.Error(exception.message ?: "미션 스팟 로드 실패")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    fun clearMissionSpots() {
        _missionSpots.value = emptyList()
    }
}