package com.example.bogoargo.di

import com.example.bogoargo.data.api.*
import com.example.bogoargo.data.storage.SecureStorage
import com.example.bogoargo.data.event.TokenExpiredEvent
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

    private const val BASE_URL = "http://i13a301.p.ssafy.io"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // 기본 OkHttpClient (토큰 갱신용 - 인터셉터 없음)
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

    // 기본 AuthApiService (토큰 갱신용)
    @Provides
    @Singleton
    @Named("basic")
    fun provideBasicAuthApiService(@Named("basic") retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTokenManagementInterceptor(
        secureStorage: SecureStorage,
        @Named("basic") authApiService: AuthApiService,
        tokenExpiredEvent: TokenExpiredEvent
    ): TokenManagementInterceptor {
        return TokenManagementInterceptor(secureStorage, authApiService, tokenExpiredEvent)
    }

    // 통합 OkHttpClient (TokenManagementInterceptor 포함)
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        tokenManagementInterceptor: TokenManagementInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(tokenManagementInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // 통합 Retrofit (일반 API용)
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 통합 AuthApiService (일반 API용)
    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTeamApiService(retrofit: Retrofit): TeamApiService {
        return retrofit.create(TeamApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideClassApiService(retrofit: Retrofit): ClassApiService {
        return retrofit.create(ClassApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideApplicationApiService(retrofit: Retrofit): AppliationApiService {
        return retrofit.create(AppliationApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideLocationApiService(retrofit: Retrofit): LocationApiService {
        return retrofit.create(LocationApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCardGameApiService(retrofit: Retrofit): CardGameApiService {
        return retrofit.create(CardGameApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMissionApiService(retrofit: Retrofit): MissionApiService {
        return retrofit.create(MissionApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideProblemApiService(retrofit: Retrofit): ProblemApiService {
        return retrofit.create(ProblemApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSpotApiService(retrofit: Retrofit): SpotApiService {
        return retrofit.create(SpotApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFCMPushApi(retrofit: Retrofit): FCMPushApi {
        return retrofit.create(FCMPushApi::class.java)
    }
}