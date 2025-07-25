package com.example.bogoargo.data.exception

sealed class AuthException(message: String) : Exception(message) {
    object TokenExpired : AuthException("토큰이 만료되었습니다")
    object InvalidCredentials : AuthException("아이디 또는 비밀번호가 잘못되었습니다")
    object NetworkError : AuthException("네트워크 연결을 확인해주세요")
    object ServerError : AuthException("서버 오류가 발생했습니다")
    object RefreshTokenExpired : AuthException("재로그인이 필요합니다")
    class UnknownError(message: String) : AuthException("알 수 없는 오류: $message")
}

fun Throwable.toAuthException(): AuthException {
    return when {
        this is AuthException -> this
        message?.contains("401") == true -> AuthException.TokenExpired
        message?.contains("Network") == true -> AuthException.NetworkError
        message?.contains("Unable to resolve host") == true -> AuthException.NetworkError
        message?.contains("timeout") == true -> AuthException.NetworkError
        else -> AuthException.UnknownError(message ?: "알 수 없는 오류")
    }
}