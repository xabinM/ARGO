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
    
    // 테스트용 더미 데이터 로드
    fun loadDummyBattleHistory(teamId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // 더미 데이터 생성
            val dummyData = createDummyBattleHistory(teamId)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                battleHistories = dummyData,
                currentPage = 0,
                totalPages = 1,
                totalElements = dummyData.size.toLong(),
                hasMorePages = false
            )
        }
    }
    
    private fun createDummyBattleHistory(teamId: Long): List<BattleHistory> {
        return listOf(
            // 1. 내가 승리한 완료된 대전 (확인됨)
            BattleHistory(
                matchId = 1L,
                challengerTeamId = teamId,
                challengedTeamId = 2L,
                challengerTeamName = "내 팀",
                challengedTeamName = "라이벌 팀",
                status = BattleStatus.COMPLETED,
                resultView = ResultView.BOTH_SEE,
                winnerTeamId = teamId,
                loserTeamId = 2L,
                myCard = BattleCard(
                    GameCard.create(1L, CardTier.LEGENDARY, 101L),
                    BattleStance.ATTACK
                ),
                opponentCard = BattleCard(
                    GameCard.create(2L, CardTier.EPIC, 202L),
                    BattleStance.DEFENSE
                ),
                createdAt = "2024-01-20T10:30:00",
                endedAt = "2024-01-20T10:35:00",
                myTeamId = teamId
            ),
            
            // 2. 내가 패배한 대전 (확인됨)
            BattleHistory(
                matchId = 2L,
                challengerTeamId = 3L,
                challengedTeamId = teamId,
                challengerTeamName = "강력한 팀",
                challengedTeamName = "내 팀",
                status = BattleStatus.COMPLETED,
                resultView = ResultView.BOTH_SEE,
                winnerTeamId = 3L,
                loserTeamId = teamId,
                myCard = BattleCard(
                    GameCard.create(3L, CardTier.RARE, 103L),
                    BattleStance.DEFENSE
                ),
                opponentCard = BattleCard(
                    GameCard.create(4L, CardTier.LEGENDARY, 304L),
                    BattleStance.ATTACK
                ),
                createdAt = "2024-01-19T14:20:00",
                endedAt = "2024-01-19T14:25:00",
                myTeamId = teamId
            ),
            
            // 3. 내가 신청한 PENDING 대전
            BattleHistory(
                matchId = 3L,
                challengerTeamId = teamId,
                challengedTeamId = 4L,
                challengerTeamName = "내 팀",
                challengedTeamName = "도전자 팀",
                status = BattleStatus.PENDING,
                resultView = ResultView.BOTH_NOT_SEE,
                winnerTeamId = null,
                loserTeamId = null,
                myCard = BattleCard(
                    GameCard.create(5L, CardTier.EPIC, 105L),
                    BattleStance.ATTACK
                ),
                opponentCard = null,
                createdAt = "2024-01-21T09:15:00",
                endedAt = null,
                myTeamId = teamId
            ),
            
            // 4. 상대가 신청한 PENDING 대전
            BattleHistory(
                matchId = 4L,
                challengerTeamId = 5L,
                challengedTeamId = teamId,
                challengerTeamName = "신규 팀",
                challengedTeamName = "내 팀",
                status = BattleStatus.PENDING,
                resultView = ResultView.BOTH_NOT_SEE,
                winnerTeamId = null,
                loserTeamId = null,
                myCard = null,
                opponentCard = BattleCard(
                    GameCard.create(7L, CardTier.RARE, 507L),
                    BattleStance.ATTACK
                ),
                createdAt = "2024-01-21T08:45:00",
                endedAt = null,
                myTeamId = teamId
            ),
            
            // 5. 완료됐지만 아직 확인하지 않은 승리 대전
            BattleHistory(
                matchId = 5L,
                challengerTeamId = teamId,
                challengedTeamId = 6L,
                challengerTeamName = "내 팀",
                challengedTeamName = "베테랑 팀",
                status = BattleStatus.COMPLETED,
                resultView = ResultView.BOTH_NOT_SEE,
                winnerTeamId = teamId,
                loserTeamId = 6L,
                myCard = BattleCard(
                    GameCard.create(8L, CardTier.RARE, 108L),
                    BattleStance.ATTACK
                ),
                opponentCard = BattleCard(
                    GameCard.create(1L, CardTier.COMMON, 601L),
                    BattleStance.DEFENSE
                ),
                createdAt = "2024-01-18T16:45:00",
                endedAt = "2024-01-18T16:50:00",
                myTeamId = teamId
            ),
            
            // 6. 만료된 대전
            BattleHistory(
                matchId = 6L,
                challengerTeamId = teamId,
                challengedTeamId = 7L,
                challengerTeamName = "내 팀",
                challengedTeamName = "타임아웃 팀",
                status = BattleStatus.EXPIRED,
                resultView = ResultView.BOTH_NOT_SEE,
                winnerTeamId = null,
                loserTeamId = null,
                myCard = BattleCard(
                    GameCard.create(2L, CardTier.EPIC, 102L),
                    BattleStance.DEFENSE
                ),
                opponentCard = null,
                createdAt = "2024-01-17T11:30:00",
                endedAt = null,
                myTeamId = teamId
            ),
            
            // 7. 취소된 대전
            BattleHistory(
                matchId = 7L,
                challengerTeamId = 8L,
                challengedTeamId = teamId,
                challengerTeamName = "포기한 팀",
                challengedTeamName = "내 팀",
                status = BattleStatus.CANCELLED,
                resultView = ResultView.BOTH_NOT_SEE,
                winnerTeamId = null,
                loserTeamId = null,
                myCard = null,
                opponentCard = null,
                createdAt = "2024-01-16T15:20:00",
                endedAt = "2024-01-16T15:21:00",
                myTeamId = teamId
            ),
            
            // 8. 무승부 대전 (공격 vs 공격)
            BattleHistory(
                matchId = 8L,
                challengerTeamId = teamId,
                challengedTeamId = 9L,
                challengerTeamName = "내 팀",
                challengedTeamName = "동등한 팀",
                status = BattleStatus.COMPLETED,
                resultView = ResultView.BOTH_SEE,
                winnerTeamId = null,
                loserTeamId = null,
                myCard = BattleCard(
                    GameCard.create(4L, CardTier.RARE, 104L),
                    BattleStance.ATTACK
                ),
                opponentCard = BattleCard(
                    GameCard.create(5L, CardTier.RARE, 905L),
                    BattleStance.ATTACK
                ),
                createdAt = "2024-01-15T13:15:00",
                endedAt = "2024-01-15T13:20:00",
                myTeamId = teamId
            ),
            
            // 9. 방어 vs 방어 무승부
            BattleHistory(
                matchId = 9L,
                challengerTeamId = 10L,
                challengedTeamId = teamId,
                challengerTeamName = "방어형 팀",
                challengedTeamName = "내 팀",
                status = BattleStatus.COMPLETED,
                resultView = ResultView.BOTH_SEE,
                winnerTeamId = null,
                loserTeamId = null,
                myCard = BattleCard(
                    GameCard.create(6L, CardTier.EPIC, 106L),
                    BattleStance.DEFENSE
                ),
                opponentCard = BattleCard(
                    GameCard.create(3L, CardTier.LEGENDARY, 1003L),
                    BattleStance.DEFENSE
                ),
                createdAt = "2024-01-14T09:45:00",
                endedAt = "2024-01-14T09:50:00",
                myTeamId = teamId
            ),
            
            // 10. 내가 방어로 승리한 대전
            BattleHistory(
                matchId = 10L,
                challengerTeamId = 11L,
                challengedTeamId = teamId,
                challengerTeamName = "공격형 팀",
                challengedTeamName = "내 팀",
                status = BattleStatus.COMPLETED,
                resultView = ResultView.BOTH_SEE,
                winnerTeamId = teamId,
                loserTeamId = 11L,
                myCard = BattleCard(
                    GameCard.create(7L, CardTier.LEGENDARY, 107L),
                    BattleStance.DEFENSE
                ),
                opponentCard = BattleCard(
                    GameCard.create(8L, CardTier.COMMON, 1108L),
                    BattleStance.ATTACK
                ),
                createdAt = "2024-01-13T19:30:00",
                endedAt = "2024-01-13T19:35:00",
                myTeamId = teamId
            ),
            
            // 11. 상대만 결과를 확인한 대전 (내가 승리)
            BattleHistory(
                matchId = 11L,
                challengerTeamId = teamId,
                challengedTeamId = 12L,
                challengerTeamName = "내 팀",
                challengedTeamName = "확인한 팀",
                status = BattleStatus.COMPLETED,
                resultView = ResultView.SEE_CHALLENGED,
                winnerTeamId = teamId,
                loserTeamId = 12L,
                myCard = BattleCard(
                    GameCard.create(1L, CardTier.EPIC, 101L),
                    BattleStance.ATTACK
                ),
                opponentCard = BattleCard(
                    GameCard.create(2L, CardTier.RARE, 1202L),
                    BattleStance.DEFENSE
                ),
                createdAt = "2024-01-12T14:15:00",
                endedAt = "2024-01-12T14:20:00",
                myTeamId = teamId
            ),
            
            // 12. 내만 결과를 확인한 대전 (내가 패배)
            BattleHistory(
                matchId = 12L,
                challengerTeamId = teamId,
                challengedTeamId = 13L,
                challengerTeamName = "내 팀",
                challengedTeamName = "미확인 팀",
                status = BattleStatus.COMPLETED,
                resultView = ResultView.SEE_CHALLENGER,
                winnerTeamId = 13L,
                loserTeamId = teamId,
                myCard = BattleCard(
                    GameCard.create(3L, CardTier.COMMON, 103L),
                    BattleStance.DEFENSE
                ),
                opponentCard = BattleCard(
                    GameCard.create(4L, CardTier.LEGENDARY, 1304L),
                    BattleStance.ATTACK
                ),
                createdAt = "2024-01-11T11:00:00",
                endedAt = "2024-01-11T11:05:00",
                myTeamId = teamId
            ),
            
            // 13. 오래된 승리 대전
            BattleHistory(
                matchId = 13L,
                challengerTeamId = 14L,
                challengedTeamId = teamId,
                challengerTeamName = "올드 팀",
                challengedTeamName = "내 팀",
                status = BattleStatus.COMPLETED,
                resultView = ResultView.BOTH_SEE,
                winnerTeamId = teamId,
                loserTeamId = 14L,
                myCard = BattleCard(
                    GameCard.create(5L, CardTier.RARE, 105L),
                    BattleStance.ATTACK
                ),
                opponentCard = BattleCard(
                    GameCard.create(6L, CardTier.EPIC, 1406L),
                    BattleStance.DEFENSE
                ),
                createdAt = "2024-01-10T16:40:00",
                endedAt = "2024-01-10T16:45:00",
                myTeamId = teamId
            ),
            
            // 14. 최신 PENDING 대전 (내가 신청)
            BattleHistory(
                matchId = 14L,
                challengerTeamId = teamId,
                challengedTeamId = 15L,
                challengerTeamName = "내 팀",
                challengedTeamName = "최신 라이벌",
                status = BattleStatus.PENDING,
                resultView = ResultView.BOTH_NOT_SEE,
                winnerTeamId = null,
                loserTeamId = null,
                myCard = BattleCard(
                    GameCard.create(7L, CardTier.LEGENDARY, 107L),
                    BattleStance.DEFENSE
                ),
                opponentCard = null,
                createdAt = "2024-01-21T12:00:00",
                endedAt = null,
                myTeamId = teamId
            ),
            
            // 15. 완료됐지만 아직 확인하지 않은 패배 대전
            BattleHistory(
                matchId = 15L,
                challengerTeamId = 16L,
                challengedTeamId = teamId,
                challengerTeamName = "강적 팀",
                challengedTeamName = "내 팀",
                status = BattleStatus.COMPLETED,
                resultView = ResultView.BOTH_NOT_SEE,
                winnerTeamId = 16L,
                loserTeamId = teamId,
                myCard = BattleCard(
                    GameCard.create(8L, CardTier.COMMON, 108L),
                    BattleStance.ATTACK
                ),
                opponentCard = BattleCard(
                    GameCard.create(1L, CardTier.LEGENDARY, 1601L),
                    BattleStance.DEFENSE
                ),
                createdAt = "2024-01-09T20:30:00",
                endedAt = "2024-01-09T20:35:00",
                myTeamId = teamId
            )
        )
    }
    
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
                totalScore = 2450,
                rank = 2
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