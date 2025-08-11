package com.example.bogoargo.ui.viewmodels.cardgame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.storage.SecureStorage
import com.example.bogoargo.domain.use_case.cardgame.GetTeamCardCollectionUseCase
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.GameCard
import com.example.bogoargo.domain.model.CardTier
import com.example.bogoargo.data.mapper.TeamCardCollection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CardCollectionUiState(
    val isLoading: Boolean = false,
    val teamCardCollection: TeamCardCollection? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class CardCollectionViewModel @Inject constructor(
    private val secureStorage: SecureStorage,
    private val getTeamCardCollectionUseCase: GetTeamCardCollectionUseCase
) : ViewModel() {
    
    // 사용자 정보는 필요시 secureStorage에서 직접 가져오기
    private fun getCurrentUser() = secureStorage.getUser()
    
    private val _uiState = MutableStateFlow(CardCollectionUiState())
    val uiState: StateFlow<CardCollectionUiState> = _uiState.asStateFlow()
    
    // 실제 API를 통한 팀 카드 컬렉션 조회
    fun loadTeamCardCollection(teamId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            when (val result = getTeamCardCollectionUseCase(teamId)) {
                is DataResult.Loading -> {
                    // 이미 로딩 상태로 설정했으므로 추가 처리 불필요
                }
                is DataResult.Success -> {
                    val collection = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        teamCardCollection = collection
                    )
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "팀 카드 컬렉션을 불러오는데 실패했습니다: ${result.exception.message}"
                    )
                }
            }
        }
    }
    
    // 테스트용 더미 데이터 로드
    fun loadDummyTeamCardCollection(teamId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // 더미 데이터 생성 (백엔드 응답 구조에 맞춤)
            val dummyCollection = createDummyTeamCardCollection(teamId)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                teamCardCollection = dummyCollection
            )
        }
    }
    
    private fun createDummyTeamCardCollection(teamId: Long): TeamCardCollection {
        val dummyCards = listOf(
            // 정상 카드들
            GameCard.create(1L, CardTier.LEGENDARY, 101L, false, false), // 불사조 (정상)
            GameCard.create(2L, CardTier.EPIC, 102L, false, false),      // 그림자 늑대 (정상)
            GameCard.create(3L, CardTier.RARE, 103L, false, false),      // 치유의 요정 (정상)
            GameCard.create(4L, CardTier.EPIC, 104L, false, false),      // 바위 골렘 (정상)
            GameCard.create(5L, CardTier.RARE, 105L, false, false),      // 번개 마법사 (정상)
            GameCard.create(6L, CardTier.COMMON, 106L, false, false),    // 숲의 수호자 (정상)
            
            // 사용 중인 카드들 (대전에 참여중)
            GameCard.create(7L, CardTier.LEGENDARY, 107L, false, true),  // 얼음 용 (사용중)
            GameCard.create(8L, CardTier.RARE, 108L, false, true),       // 기사 (사용중)
            GameCard.create(1L, CardTier.COMMON, 109L, false, true),     // 불사조 (일반, 사용중)
            
            // 제거된 카드들
            GameCard.create(2L, CardTier.RARE, 110L, true, false),       // 그림자 늑대 (레어, 제거됨)
            GameCard.create(3L, CardTier.EPIC, 111L, true, false),       // 치유의 요정 (에픽, 제거됨)
            
            // 추가 정상 카드들
            GameCard.create(4L, CardTier.LEGENDARY, 112L, false, false), // 바위 골렘 (전설, 정상)
            GameCard.create(5L, CardTier.COMMON, 113L, false, false),    // 번개 마법사 (일반, 정상)
            GameCard.create(6L, CardTier.RARE, 114L, false, false),      // 숲의 수호자 (레어, 정상)
            GameCard.create(7L, CardTier.EPIC, 115L, false, false),      // 얼음 용 (에픽, 정상)
            GameCard.create(8L, CardTier.LEGENDARY, 116L, false, false), // 기사 (전설, 정상)
        )
        
        // 등급별 통계 계산
        val tierStats = dummyCards.groupBy { it.rarity.name }.mapValues { it.value.size }
        
        return TeamCardCollection(
            teamId = teamId,
            cards = dummyCards,
            totalCount = dummyCards.size,
            tierStats = tierStats
        )
    }
    
    // 오류 메시지 클리어
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}