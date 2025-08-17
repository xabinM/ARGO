package com.example.bogoargo.domain.model

sealed class DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>()
    data class Error(val exception: DataException) : DataResult<Nothing>()
    data object Loading : DataResult<Nothing>()
}

sealed class DataException(override val message: String) : Exception(message) {
    data object NetworkError : DataException("네트워크 연결에 실패했습니다")
    data object ServerError : DataException("서버 오류가 발생했습니다")
    data object AuthenticationError : DataException("인증에 실패했습니다")
    data object UnauthorizedError : DataException("권한이 없습니다")
    data object NotFoundError : DataException("요청한 데이터를 찾을 수 없습니다")
    data object InvalidCredentialsError : DataException("아이디 또는 비밀번호가 일치하지 않습니다")
    data class ValidationError(val field: String) : DataException("$field 검증에 실패했습니다")
    data class UnknownError(val originalMessage: String) : DataException("알 수 없는 오류: $originalMessage")
}

inline fun <T> DataResult<T>.onSuccess(action: (T) -> Unit): DataResult<T> {
    if (this is DataResult.Success) action(data)
    return this
}

inline fun <T> DataResult<T>.onError(action: (DataException) -> Unit): DataResult<T> {
    if (this is DataResult.Error) action(exception)
    return this
}

inline fun <T> DataResult<T>.onLoading(action: () -> Unit): DataResult<T> {
    if (this is DataResult.Loading) action()
    return this
}