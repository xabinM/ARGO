package com.example.bogoargo.ui.viewmodels.cardgame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.preferences.PreferencesManager
import com.example.bogoargo.domain.use_case.cardgame.GetBattleOpponentsUseCase
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.BattleOpponent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BattleRequestUiState(
    val isLoading: Boolean = false,
    val battleOpponents: List<BattleOpponent> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class BattleRequestViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val getBattleOpponentsUseCase: GetBattleOpponentsUseCase
) : ViewModel() {
    
    val currentUserId: StateFlow<Long?> = preferencesManager.currentUserId
    val currentUserName: StateFlow<String?> = preferencesManager.currentUserName
    val currentUserRole: StateFlow<String?> = preferencesManager.currentUserRole
    
    private val _uiState = MutableStateFlow(BattleRequestUiState())
    val uiState: StateFlow<BattleRequestUiState> = _uiState.asStateFlow()
    
    // 대전 상대 팀 목록 로드 (실제 API)
    fun loadBattleOpponents(teamId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = getBattleOpponentsUseCase(teamId)) {
                is DataResult.Loading -> {
                    // 이미 로딩 상태로 설정했으므로 추가 처리 불필요
                }
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        battleOpponents = result.data
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "대전 상대 목록을 불러오는데 실패했습니다: ${result.exception.message}"
                    )
                }
            }
        }
    }
    
    // 테스트용 더미 데이터 로드
    fun loadDummyBattleOpponents(teamId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // 더미 데이터 생성
            val dummyOpponents = createDummyBattleOpponents(teamId)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                battleOpponents = dummyOpponents
            )
        }
    }
    
    // 오류 메시지 클리어
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    private fun createDummyBattleOpponents(teamId: Long): List<BattleOpponent> {
        return listOf(
            BattleOpponent(
                teamId = 2L,
                teamName = "불사조 팀 🔥",
                leaderName = "김민수",
                totalGames = 15,
                wins = 10,
                losses = 4,
                draws = 1,
                totalPoints = 2100
            ),
            BattleOpponent(
                teamId = 3L,
                teamName = "그리핀 팀 🦅",
                leaderName = "이지은",
                totalGames = 12,
                wins = 7,
                losses = 4,
                draws = 1,
                totalPoints = 1850
            ),
            BattleOpponent(
                teamId = 4L,
                teamName = "유니콘 팀 🦄",
                leaderName = "박준호",
                totalGames = 20,
                wins = 15,
                losses = 3,
                draws = 2,
                totalPoints = 2300
            ),
            BattleOpponent(
                teamId = 5L,
                teamName = "드래곤 팀 🐉",
                leaderName = "최서연",
                totalGames = 18,
                wins = 14,
                losses = 2,
                draws = 2,
                totalPoints = 2450
            ),
            BattleOpponent(
                teamId = 6L,
                teamName = "피닉스 팀 🔥",
                leaderName = "정하윤",
                totalGames = 8,
                wins = 5,
                losses = 2,
                draws = 1,
                totalPoints = 1200
            ),
            BattleOpponent(
                teamId = 7L,
                teamName = "사자 팀 🦁",
                leaderName = "임도현",
                totalGames = 10,
                wins = 3,
                losses = 6,
                draws = 1,
                totalPoints = 950
            )
        ).filter { it.teamId != teamId } // 현재 팀은 제외
    }
}