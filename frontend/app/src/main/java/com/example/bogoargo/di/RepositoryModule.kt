package com.example.bogoargo.di

import android.content.Context
import com.example.bogoargo.data.api.*
import com.example.bogoargo.data.repository.*
import com.example.bogoargo.data.storage.TokenStorage
import com.example.bogoargo.domain.repository.*
import javax.inject.Named
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): IAuthRepository

    @Binds
    @Singleton  
    abstract fun bindUserRepository(impl: UserRepositoryImpl): IUserRepository

    @Binds
    @Singleton
    abstract fun bindTeamRepository(impl: TeamRepositoryImpl): ITeamRepository

    @Binds
    @Singleton
    abstract fun bindClassRepository(impl: ClassRepositoryImpl): IClassRepository
    
    companion object {
        
        @Provides
        @Singleton
        fun provideMissionRepository(
            @Named("authenticated") authApiService: AuthApiService,
            @ApplicationContext context: Context
        ): MissionRepository {
            return MissionRepository(authApiService, context)
        }
        
        @Provides
        @Singleton
        fun provideAR3DObjectRepository(): AR3DObjectRepository {
            return AR3DObjectRepository
        }
        
        @Provides
        @Singleton
        fun provideSettingsRepository(
            @ApplicationContext context: Context
        ): SettingsRepository {
            return SettingsRepository(context)
        }
    }
}