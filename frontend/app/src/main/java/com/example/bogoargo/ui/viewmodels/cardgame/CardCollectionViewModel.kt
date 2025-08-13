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
    
    // 삭제됨: 더미 데이터 로드 함수 제거
    
    // 삭제됨: 더미 데이터 생성 함수 제거
    
    // 오류 메시지 클리어
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}