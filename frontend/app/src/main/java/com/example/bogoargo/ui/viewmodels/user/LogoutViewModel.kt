package com.example.bogoargo.ui.viewmodels.user

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bogoargo.domain.use_case.auth.LogoutUseCase
import androidx.work.WorkManager
import com.example.bogoargo.domain.repository.IAuthRepository
import com.example.bogoargo.domain.repository.IUserRepository
import com.example.bogoargo.data.repository.FCMPushSender
import com.example.bogoargo.worker.LocationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogoutUiState(
    val isLoading: Boolean = false,
    val isLoggedOut: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LogoutViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: IUserRepository,
    private val authRepository: IAuthRepository,
    private val logoutUseCase: LogoutUseCase,
    private val pushSender: FCMPushSender
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogoutUiState())
    val uiState: StateFlow<LogoutUiState> = _uiState

    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                clearUserSession()
                
                // WorkManager 위치 추적 중단
                stopLocationTracking()
                pushSender.unregisterToken()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedOut = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    private suspend fun clearUserSession() {
        logoutUseCase.invoke()
    }

    fun clearState() {
        _uiState.value = LogoutUiState()
    }

    private fun stopLocationTracking() {
        // WorkManager에서 위치 추적 작업 취소
        WorkManager.getInstance(context)
            .cancelUniqueWork(LocationWorker.WORK_NAME)
    }
}