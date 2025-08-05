package com.example.bogoargo.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.bogoargo.data.api.AuthApiService
import com.example.bogoargo.domain.model.DataException
import com.example.bogoargo.domain.model.DataResult
import com.example.bogoargo.domain.model.RefreshTokenRequest
import com.example.bogoargo.domain.model.TokenInfo
import com.example.bogoargo.data.storage.TokenStorage
import com.example.bogoargo.domain.repository.IAuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("basic") private val authApiService: AuthApiService,
    private val tokenStorage: TokenStorage
) : IAuthRepository {
    
    private val refreshMutex = Mutex()


//TODO: 유저 저장 기능
//    suspend fun saveAuthInfo(jwtToken: String, user: UserLoginResponse) {
//        preferencesManager.saveJwtToken(jwtToken)
//        preferencesManager.saveUserId(user.data?.userId ?: -1L)
//        preferencesManager.saveUserName(user.data?.name ?: "")
//        preferencesManager.saveUserRole(user.data?.role.toString())
//    }

    
    override fun getTokenInfo(): TokenInfo? {
        val accessToken = tokenStorage.getAccessToken()
        val refreshToken = tokenStorage.getRefreshToken()
        
        return if (accessToken != null && refreshToken != null) {
            TokenInfo(accessToken, refreshToken)
        } else {
            null
        }
    }
    
    override fun saveTokens(accessToken: String, refreshToken: String) {
        tokenStorage.saveTokens(accessToken, refreshToken)
    }
    
    override fun clearTokens() {
        tokenStorage.clearTokens()
    }
    
    override fun getAccessToken(): String? {
        return tokenStorage.getAccessToken()
    }
    
    override fun getRefreshToken(): String? {
        return tokenStorage.getRefreshToken()
    }
    
    override fun hasTokens(): Boolean {
        return tokenStorage.hasTokens()
    }
    
    override suspend fun refreshToken(): DataResult<Boolean> {
        return refreshMutex.withLock {
            val refreshToken = getRefreshToken() 
                ?: return@withLock DataResult.Error(DataException.AuthenticationError)
            
            try {
                val response = authApiService.refreshToken(
                    RefreshTokenRequest(refreshToken)
                )
                
                if (response.isSuccessful) {
                    val refreshResponse = response.body()
                    if (refreshResponse != null) {
                        saveTokens(refreshResponse.accessToken, refreshResponse.refreshToken)
                        return@withLock DataResult.Success(true)
                    }
                }
                
                clearTokens()
                return@withLock DataResult.Error(DataException.AuthenticationError)
            } catch (e: IOException) {
                clearTokens()
                return@withLock DataResult.Error(DataException.NetworkError)
            } catch (e: HttpException) {
                clearTokens()
                return@withLock DataResult.Error(
                    when (e.code()) {
                        401 -> DataException.AuthenticationError
                        403 -> DataException.UnauthorizedError
                        404 -> DataException.NotFoundError
                        else -> DataException.ServerError
                    }
                )
            } catch (e: Exception) {
                clearTokens()
                return@withLock DataResult.Error(DataException.UnknownError(e.message ?: "Unknown error"))
            }
        }
    }
}