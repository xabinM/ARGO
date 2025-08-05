package com.example.bogoargo.di

import com.example.bogoargo.domain.repository.IAuthRepository
import com.example.bogoargo.domain.repository.IClassRepository
import com.example.bogoargo.domain.repository.ITeamRepository
import com.example.bogoargo.domain.repository.IUserRepository
import com.example.bogoargo.domain.use_case.auth.LoginUseCase
import com.example.bogoargo.domain.use_case.auth.LogoutUseCase
import com.example.bogoargo.domain.use_case.auth.RefreshTokenUseCase
import com.example.bogoargo.domain.use_case.auth.SaveTokensUseCase
import com.example.bogoargo.domain.use_case.classroom.ApplyClassUseCase
import com.example.bogoargo.domain.use_case.classroom.CreateClassUseCase
import com.example.bogoargo.domain.use_case.classroom.DeleteClassUseCase
import com.example.bogoargo.domain.use_case.classroom.GetClassDetailUseCase
import com.example.bogoargo.domain.use_case.classroom.GetClassesUseCase
import com.example.bogoargo.domain.use_case.classroom.GetStudentClassListUseCase
import com.example.bogoargo.domain.use_case.classroom.GetTeacherClassListUseCase
import com.example.bogoargo.domain.use_case.classroom.JoinClassUseCase
import com.example.bogoargo.domain.use_case.classroom.LeaveClassUseCase
import com.example.bogoargo.domain.use_case.team.CreateTeamUseCase
import com.example.bogoargo.domain.use_case.team.ManageTeamUseCase
import com.example.bogoargo.domain.use_case.user.SignUpUseCase
import com.example.bogoargo.domain.use_case.user.UpdateUserProfileUseCase
import com.example.bogoargo.domain.use_case.user.WithdrawUserUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // Auth Use Cases
    @Provides
    @Singleton
    fun provideLoginUseCase(userRepository: IUserRepository): LoginUseCase {
        return LoginUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideLogoutUseCase(authRepository: IAuthRepository): LogoutUseCase {
        return LogoutUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideRefreshTokenUseCase(authRepository: IAuthRepository): RefreshTokenUseCase {
        return RefreshTokenUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideSaveTokensUseCase(authRepository: IAuthRepository): SaveTokensUseCase {
        return SaveTokensUseCase(authRepository)
    }

    // User Use Cases
    @Provides
    @Singleton
    fun provideSignUpUseCase(userRepository: IUserRepository): SignUpUseCase {
        return SignUpUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateUserProfileUseCase(userRepository: IUserRepository): UpdateUserProfileUseCase {
        return UpdateUserProfileUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideWithdrawUserUseCase(userRepository: IUserRepository): WithdrawUserUseCase {
        return WithdrawUserUseCase(userRepository)
    }

    // Team Use Cases
    @Provides
    @Singleton
    fun provideCreateTeamUseCase(teamRepository: ITeamRepository): CreateTeamUseCase {
        return CreateTeamUseCase(teamRepository)
    }

    @Provides
    @Singleton
    fun provideManageTeamUseCase(teamRepository: ITeamRepository): ManageTeamUseCase {
        return ManageTeamUseCase(teamRepository)
    }

    // Class Use Cases
    @Provides
    @Singleton
    fun provideCreateClassUseCase(classRepository: IClassRepository): CreateClassUseCase {
        return CreateClassUseCase(classRepository)
    }

    @Provides
    @Singleton
    fun provideGetClassesUseCase(classRepository: IClassRepository): GetClassesUseCase {
        return GetClassesUseCase(classRepository)
    }

    @Provides
    @Singleton
    fun provideGetClassDetailUseCase(classRepository: IClassRepository): GetClassDetailUseCase {
        return GetClassDetailUseCase(classRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteClassUseCase(classRepository: IClassRepository): DeleteClassUseCase {
        return DeleteClassUseCase(classRepository)
    }

    @Provides
    @Singleton
    fun provideJoinClassUseCase(classRepository: IClassRepository): JoinClassUseCase {
        return JoinClassUseCase(classRepository)
    }

    @Provides
    @Singleton
    fun provideLeaveClassUseCase(classRepository: IClassRepository): LeaveClassUseCase {
        return LeaveClassUseCase(classRepository)
    }

    @Provides
    @Singleton
    fun provideApplyClassUseCase(classRepository: IClassRepository): ApplyClassUseCase {
        return ApplyClassUseCase(classRepository)
    }

    @Provides
    @Singleton
    fun provideGetTeacherClassListUseCase(classRepository: IClassRepository): GetTeacherClassListUseCase {
        return GetTeacherClassListUseCase(classRepository)
    }

    @Provides
    @Singleton
    fun provideGetStudentClassListUseCase(classRepository: IClassRepository): GetStudentClassListUseCase {
        return GetStudentClassListUseCase(classRepository)
    }
}