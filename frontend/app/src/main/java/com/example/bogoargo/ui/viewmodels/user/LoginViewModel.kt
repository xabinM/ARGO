package com.example.bogoargo.ui.viewmodels.user

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.bogoargo.data.storage.SecureStorage
import com.example.bogoargo.data.repository.FCMPushSender
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.User
import com.example.bogoargo.domain.model.UserRole
import com.example.bogoargo.domain.use_case.auth.LoginUseCase
import com.example.bogoargo.domain.use_case.auth.SaveTokensUseCase
import com.example.bogoargo.navigation.Screen
import com.example.bogoargo.worker.LocationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import com.google.firebase.messaging.FirebaseMessaging

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val username: String = "",
    val password: String = "",
    val user: User? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val loginUseCase: LoginUseCase,
    private val saveTokensUseCase: SaveTokensUseCase,
    private val secureStorage: SecureStorage,
    private val pushSender: FCMPushSender
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _navigationEvent = MutableSharedFlow<Screen>()
    val navigationEvent: SharedFlow<Screen> = _navigationEvent.asSharedFlow()

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun login() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = loginUseCase(_uiState.value.username, _uiState.value.password)) {
                is DataResult.Success -> {
                    // 사용자 정보는 이미 UserRepository에서 저장됨 (중복 저장 제거)

                    // WorkManager로 주기적 위치 추적 시작
                    startLocationTracking()

                    login_fcm_loading()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        user = result.data
                    )

                    // 로그인 성공 시 SelectHome으로 이동
                    _navigationEvent.emit(Screen.SelectHome)
                }
                is DataResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is DataResult.Loading -> {
                    // 이미 로딩 상태 설정됨
                }
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearState() {
        _uiState.value = LoginUiState()
    }

    suspend fun getLoggedInUser(): User? {
        // SecureStorage에서 사용자 정보 복원
        return secureStorage.getUser()
    }


    private fun startLocationTracking() {
        // 제약 조건 설정: 네트워크 연결 시에만 실행
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 15분마다 실행되는 주기적 작업 요청 생성 (Android 최소 간격)
        val locationWorkRequest = PeriodicWorkRequestBuilder<LocationWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        // WorkManager에 작업 등록 (중복 방지)
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                LocationWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                locationWorkRequest
            )
    }

    private fun login_fcm_loading() {
        val cached = secureStorage.getFcmToken()
        if (!cached.isNullOrBlank()) {
            pushSender.sendCurrentToken()
            return
        }

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                secureStorage.saveFcmToken(token)
                pushSender.sendCurrentToken()
            }
    }
}