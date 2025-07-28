package com.example.bogoargo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.data.model.User
import com.example.bogoargo.data.model.UserRole
import com.example.bogoargo.data.repository.AuthRepository
import com.example.bogoargo.data.repository.UserRepository
import kotlinx.coroutines.launch

class TeacherMainViewModel(
    private val tokenManager: AuthRepository? = null, // DI로 주입받는다고 가정
    private val userRepository: UserRepository? = null // DI로 주입받는다고 가정
) : ViewModel() {

    // 유저 정보를 저장할 상태 변수
    private val _user = mutableStateOf<User?>(null)
    val user: State<User?> = _user

    // 로딩 상태를 나타내는 변수
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // UI에 표시할 상태 메시지
    private val _uiState = mutableStateOf<UiState>(UiState.Loading)
    val uiState: State<UiState> = _uiState

    // UI 상태를 표현하는 sealed class
    sealed class UiState {
        object Loading : UiState() // 로딩 중
        object Authenticated : UiState() // 인증 성공
        object Unauthenticated : UiState() // 로그인 필요
        object Unauthorized : UiState() // 권한 없음
        data class Error(val message: String) : UiState() // 기타 에러
    }

    init {
        // ViewModel이 생성될 때 유저 정보를 가져오는 함수 호출
        fetchTeacherInfo()
    }

}

    private fun fetchTeacherInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = UiState.Loading

            try {
                // Repository가 없는 경우 임시 데이터 사용
                if (tokenManager == null || userRepository == null) {
                    // 임시 교사 데이터
                    val mockTeacherUser = User(
                        id = "teacher_1",
                        userName = "김싸피",
                        email = "teacher@ssafy.com",
                        role = UserRole.TEACHER,
                        studentId = "T2025001",
                        phoneNumber = "010-1234-5678",
                        bio = "SSAFY 교육생들을 지도하는 교사입니다."
                    )
                    
                    kotlinx.coroutines.delay(1000) // 로딩 시뮬레이션
                    _user.value = mockTeacherUser
                    _uiState.value = UiState.Authenticated
                    return@launch
                }

                // 1. 저장된 토큰 가져오기 (로그인 상태 확인)
                val token = tokenManager.getToken()
                if (token.isNullOrBlank()) {
                    // 토큰이 없으므로 로그인 페이지로 이동해야 함
                    _uiState.value = UiState.Unauthenticated
                    return@launch
                }

                // 2. 서버에서 유저 정보 가져오기
                val userInfo = userRepository.getMyInfo(token)

                // 3. 역할(role) 확인
                if (userInfo.role != UserRole.TEACHER) {
                    // 역할이 'teacher'가 아니므로 접근 권한 없음
                    _uiState.value = UiState.Unauthorized
                    return@launch
                }

                // 4. 모든 검증 성공, 유저 정보 업데이트
                _user.value = userInfo
                _uiState.value = UiState.Authenticated

            } catch (e: Exception) {
                // 서버 통신 오류 등 예외 처리
                _uiState.value = UiState.Error("사용자 정보를 가져오는 데 실패했습니다: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

