package com.example.bogoargo.di

import com.example.bogoargo.data.api.*
import com.example.bogoargo.data.storage.TokenStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private const val BASE_URL = "http://localhost:8080/"
    
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    @Provides
    @Singleton
    fun provideAuthHeaderInterceptor(tokenStorage: TokenStorage): AuthHeaderInterceptor {
        return AuthHeaderInterceptor(tokenStorage)
    }
    
    @Provides
    @Singleton
    fun provideTokenAuthenticator(tokenStorage: TokenStorage, @Named("basic") authApiService: AuthApiService): TokenAuthenticator {
        return TokenAuthenticator(tokenStorage, authApiService)
    }
    
    // 기본 OkHttpClient (인증 없음)
    @Provides
    @Singleton
    @Named("basic")
    fun provideBasicOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }
    
    // 인증 OkHttpClient (TokenAuthenticator 포함)
    @Provides
    @Singleton
    @Named("authenticated")
    fun provideAuthenticatedOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authHeaderInterceptor: AuthHeaderInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authHeaderInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(loggingInterceptor)
            .build()
    }
    
    // 기본 Retrofit (토큰 갱신용)
    @Provides
    @Singleton
    @Named("basic")
    fun provideBasicRetrofit(@Named("basic") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // 인증 Retrofit (일반 API용)
    @Provides
    @Singleton
    @Named("authenticated")
    fun provideAuthenticatedRetrofit(@Named("authenticated") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // 기본 AuthApiService (토큰 갱신용)
    @Provides
    @Singleton
    @Named("basic")
    fun provideBasicAuthApiService(@Named("basic") retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
    
    // 인증 AuthApiService (일반 API용)
    @Provides
    @Singleton
    @Named("authenticated")
    fun provideAuthenticatedAuthApiService(@Named("authenticated") retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideTeamApiService(@Named("authenticated") retrofit: Retrofit): TeamApiService {
        return retrofit.create(TeamApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideClassApiService(@Named("authenticated") retrofit: Retrofit): ClassApiService {
        return retrofit.create(ClassApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideUserApiService(@Named("authenticated") retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideApplicationApiService(@Named("authenticated") retrofit: Retrofit): AppliationApiService {
        return retrofit.create(AppliationApiService::class.java)
    }
}