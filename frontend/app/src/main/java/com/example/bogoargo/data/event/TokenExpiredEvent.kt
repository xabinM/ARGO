package com.example.bogoargo.data.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenExpiredEvent @Inject constructor() {
    private val _tokenExpiredFlow = MutableSharedFlow<Unit>()
    val tokenExpiredFlow: SharedFlow<Unit> = _tokenExpiredFlow.asSharedFlow()
    
    fun notifyTokenExpired() {
        _tokenExpiredFlow.tryEmit(Unit)
    }
}