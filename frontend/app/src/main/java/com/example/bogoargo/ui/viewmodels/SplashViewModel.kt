package com.example.bogoargo.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.bogoargo.data.api.AuthApiService
import com.example.bogoargo.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SplashViewModel @Inject constructor(
    val authRepository: IAuthRepository,
    @Named("authenticated") private val authApiService: AuthApiService
) : ViewModel() {
    
    suspend fun validateToken(): Response<Unit> {
        return authApiService.validateToken()
    }
}