package com.example.bogoargo.data.api

import android.content.Context
import com.example.bogoargo.data.repository.AuthRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://localhost:8080/"
    
    private lateinit var authRepository: AuthRepository
    
    fun init(context: Context) {
        authRepository = AuthRepository(context)
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val authHeaderInterceptor by lazy {
        AuthHeaderInterceptor(authRepository)
    }
    
    private val tokenAuthenticator by lazy {
        TokenAuthenticator(authRepository)
    }
    
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authHeaderInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(loggingInterceptor)
            .build()
    }
    
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val authApiService: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
}