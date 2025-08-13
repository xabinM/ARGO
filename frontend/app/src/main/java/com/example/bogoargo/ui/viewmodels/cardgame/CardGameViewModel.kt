package com.example.bogoargo.ui.viewmodels.cardgame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.storage.SecureStorage
import com.example.bogoargo.domain.use_case.cardgame.GetBattleHistoryUseCase
import com.example.bogoargo.domain.use_case.cardgame.CancelBattleUseCase
import com.example.bogoargo.domain.use_case.cardgame.ViewBattleResultUseCase
import com.example.bogoargo.domain.use_case.cardgame.RespondToBattleUseCase
import com.example.bogoargo.domain.use_case.cardgame.GetTeamStatsUseCase
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.BattleHistory
import com.example.bogoargo.domain.model.BattleStatus
import com.example.bogoargo.domain.model.ResultView
import com.example.bogoargo.domain.model.BattleCard
import com.example.bogoargo.domain.model.GameCard
import com.example.bogoargo.domain.model.CardTier
import com.example.bogoargo.domain.model.BattleStance
import com.example.bogoargo.domain.model.TeamCardStats
import com.example.bogoargo.data.mapper.BattleHistoryPagination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CardGameUiState(
    val isLoading: Boolean = false,
    val battleHistories: List<BattleHistory> = emptyList(),
    val errorMessage: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val hasMorePages: Boolean = false,
    val isCancelLoading: Boolean = false,
    val cancelResult: String? = null,
    val cancelError: String? = null,
    val isViewResultLoading: Boolean = false,
    val viewResultSuccess: Boolean = false,
    val viewResultError: String? = null,
    val isRejectLoading: Boolean = false,
    val rejectResult: String? = null,
    val rejectError: String? = null,
    val teamStats: TeamCardStats? = null,
    val isStatsLoading: Boolean = false,
    val statsError: String? = null
)

@HiltViewModel
class CardGameViewModel @Inject constructor(
    private val secureStorage: SecureStorage,
    private val getBattleHistoryUseCase: GetBattleHistoryUseCase,
    private val cancelBattleUseCase: CancelBattleUseCase,
    private val viewBattleResultUseCase: ViewBattleResultUseCase,
    private val respondToBattleUseCase: RespondToBattleUseCase,
    private val getTeamStatsUseCase: GetTeamStatsUseCase
) : ViewModel() {
    
    // 사용자 정보는 필요시 secureStorage에서 직접 가져오기
    private fun getCurrentUser() = secureStorage.getUser()
    
    private val _uiState = MutableStateFlow(CardGameUiState())
    val uiState: StateFlow<CardGameUiState> = _uiState.asStateFlow()
    
    fun isTeamLeader(leaderId: Long): Boolean {
        val currentUser = getCurrentUser()
        return currentUser != null && currentUser.userId == leaderId
    }
    
    // 실제 API를 통한 대전 기록 조회
    fun loadBattleHistory(teamId: Long, page: Int = 0, size: Int = 10) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = getBattleHistoryUseCase(teamId, page, size)) {
                is DataResult.Loading -> {
                    // 이미 위에서 로딩 상태로 설정했으므로 추가 처리 불필요
                }
                is DataResult.Success -> {
                    val pagination = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        battleHistories = if (page == 0) pagination.battles else _uiState.value.battleHistories + pagination.battles,
                        currentPage = pagination.currentPage,
                        totalPages = pagination.totalPages,
                        totalElements = pagination.totalElements,
                        hasMorePages = pagination.currentPage < pagination.totalPages - 1
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "대전 기록을 불러오는데 실패했습니다: ${result.exception.message}"
                    )
                }
            }
        }
    }
    
    // 삭제됨: 더미 데이터 관련 함수들 제거
    
    // 페이지네이션 관련 함수들

    
    // 페이지 더 로드하기
    fun loadMoreBattleHistory(teamId: Long) {
        val currentState = _uiState.value
        if (!currentState.isLoading && currentState.hasMorePages) {
            loadBattleHistory(teamId, currentState.currentPage + 1)
        }
    }
    
    // 새로고침
    fun refreshBattleHistory(teamId: Long) {
        loadBattleHistory(teamId, 0)
    }
    
    // 오류 메시지 클리어
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    // 대전 신청 취소
    fun cancelBattle(matchId: Long, teamId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCancelLoading = true, cancelError = null, cancelResult = null)
            
            when (val result = cancelBattleUseCase(matchId)) {
                is DataResult.Loading -> {
                    // 이미 로딩 상태로 설정했으므로 추가 처리 불필요
                }
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isCancelLoading = false,
                        cancelResult = result.data.message
                    )
                    // 취소 후 대전 기록 새로고침
                    refreshBattleHistory(teamId)
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isCancelLoading = false,
                        cancelError = "대전 신청 취소에 실패했습니다: ${result.exception.message}"
                    )
                }
            }
        }
    }
    
    // 취소 상태 클리어
    fun clearCancelStatus() {
        _uiState.value = _uiState.value.copy(
            cancelResult = null, 
            cancelError = null
        )
    }
    
    // 대전 결과 확인
    fun viewBattleResult(matchId: Long, onNavigateToResult: (matchId: Long) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isViewResultLoading = true,
                viewResultSuccess = false,
                viewResultError = null
            )
            
            when (val result = viewBattleResultUseCase(matchId)) {
                is DataResult.Loading -> {
                    // 이미 로딩 상태로 설정했으므로 추가 처리 불필요
                }
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isViewResultLoading = false,
                        viewResultSuccess = true
                    )
                    // API 호출이 성공했을 때만 결과 화면으로 네비게이션
                    onNavigateToResult(matchId)
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isViewResultLoading = false,
                        viewResultError = "대전 결과 확인에 실패했습니다: ${result.exception.message}"
                    )
                }
            }
        }
    }
    
    // 결과 확인 상태 클리어
    fun clearViewResultStatus() {
        _uiState.value = _uiState.value.copy(
            viewResultSuccess = false,
            viewResultError = null
        )
    }
    
    // 대전 거절
    fun rejectBattle(matchId: Long, teamId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRejectLoading = true,
                rejectError = null,
                rejectResult = null
            )
            
            when (val result = respondToBattleUseCase(matchId, "reject")) {
                is DataResult.Loading -> {
                    // 이미 로딩 상태로 설정했으므로 추가 처리 불필요
                }
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isRejectLoading = false,
                        rejectResult = result.data.message
                    )
                    // 거절 후 대전 기록 새로고침
                    refreshBattleHistory(teamId)
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isRejectLoading = false,
                        rejectError = "대전 거절에 실패했습니다: ${result.exception.message}"
                    )
                }
            }
        }
    }
    
    // 거절 상태 클리어
    fun clearRejectStatus() {
        _uiState.value = _uiState.value.copy(
            rejectResult = null,
            rejectError = null
        )
    }
    
    // 팀 통계 로드
    fun loadTeamStats(teamId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isStatsLoading = true, statsError = null)
            
            when (val result = getTeamStatsUseCase(teamId)) {
                is DataResult.Loading -> {
                    // 이미 로딩 상태로 설정했으므로 추가 처리 불필요
                }
                is DataResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isStatsLoading = false,
                        teamStats = result.data
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isStatsLoading = false,
                        statsError = "팀 통계를 불러오는데 실패했습니다: ${result.exception.message}"
                    )
                }
            }
        }
    }
    
    // 테스트용 더미 통계 로드
    fun loadDummyTeamStats(teamId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isStatsLoading = true, statsError = null)
            
            // 더미 데이터 생성
            val dummyStats = TeamCardStats(
                teamId = teamId,
                teamName = "드래곤 슬레이어",
                wins = 12,
                losses = 3,
                totalScore = 2450
            )
            
            _uiState.value = _uiState.value.copy(
                isStatsLoading = false,
                teamStats = dummyStats
            )
        }
    }
    
    // 통계 에러 클리어
    fun clearStatsError() {
        _uiState.value = _uiState.value.copy(statsError = null)
    }
}