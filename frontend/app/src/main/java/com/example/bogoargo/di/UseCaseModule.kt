package com.example.bogoargo.di

import com.example.bogoargo.data.repository.AR3DObjectRepository
import com.example.bogoargo.domain.repository.IAuthRepository
import com.example.bogoargo.domain.repository.IClassRepository
import com.example.bogoargo.domain.repository.ILocationRepository
import com.example.bogoargo.domain.repository.IMissionRepository
import com.example.bogoargo.domain.repository.ISettingsRepository
import com.example.bogoargo.domain.repository.ITeamRepository
import com.example.bogoargo.domain.repository.IUserRepository
import com.example.bogoargo.domain.repository.ICardGameRepository
import com.example.bogoargo.domain.repository.IProblemRepository
import com.example.bogoargo.domain.repository.ISpotRepository
import com.example.bogoargo.domain.use_case.auth.LoginUseCase
import com.example.bogoargo.domain.use_case.auth.LogoutUseCase
import com.example.bogoargo.domain.use_case.auth.RefreshTokenUseCase
import com.example.bogoargo.domain.use_case.auth.SaveTokensUseCase
import com.example.bogoargo.domain.use_case.classroom.ApplyClassUseCase
import com.example.bogoargo.domain.use_case.classroom.ApproveApplicationUseCase
import com.example.bogoargo.domain.use_case.classroom.CreateClassUseCase
import com.example.bogoargo.domain.use_case.classroom.DeleteClassUseCase
import com.example.bogoargo.domain.use_case.classroom.GetApplicationListUseCase
import com.example.bogoargo.domain.use_case.classroom.GetClassDetailUseCase
import com.example.bogoargo.domain.use_case.classroom.GetClassMemberListUseCase
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
import com.example.bogoargo.domain.use_case.mission.GetMissionSpotsUseCase
import com.example.bogoargo.domain.use_case.mission.GetMissionUseCase
import com.example.bogoargo.domain.use_case.mission.ManageMissionUseCase
import com.example.bogoargo.domain.use_case.settings.GetSettingsUseCase
import com.example.bogoargo.domain.use_case.settings.UpdateSettingsUseCase
import com.example.bogoargo.domain.use_case.ar.GetAR3DObjectsUseCase
import com.example.bogoargo.domain.use_case.location.getLocationsUseCase
import com.example.bogoargo.domain.use_case.cardgame.GetBattleHistoryUseCase
import com.example.bogoargo.domain.use_case.cardgame.GetTeamCardCollectionUseCase
import com.example.bogoargo.domain.use_case.cardgame.CreateBattleUseCase
import com.example.bogoargo.domain.use_case.cardgame.RespondToBattleUseCase
import com.example.bogoargo.domain.use_case.cardgame.GetBattleOpponentsUseCase
import com.example.bogoargo.domain.use_case.cardgame.CancelBattleUseCase
import com.example.bogoargo.domain.use_case.cardgame.ViewBattleResultUseCase
import com.example.bogoargo.domain.use_case.cardgame.GetTeamStatsUseCase
import com.example.bogoargo.domain.use_case.problem.GenerateProblemUseCase
import com.example.bogoargo.domain.use_case.problem.RegisterProblemUseCase
import com.example.bogoargo.domain.use_case.spot.GetSpotListUseCase
import com.example.bogoargo.data.repository.MissionRepositoryImpl
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

    @Provides
    @Singleton
    fun provideGetApplicationListUseCase(classRepository: IClassRepository): GetApplicationListUseCase {
        return GetApplicationListUseCase(classRepository)
    }

    @Provides
    @Singleton
    fun provideApproveApplicationUseCase(classRepository: IClassRepository): ApproveApplicationUseCase {
        return ApproveApplicationUseCase(classRepository)
    }

    @Provides
    @Singleton
    fun provideGetClassMemberListUseCase(classRepository: IClassRepository): GetClassMemberListUseCase {
        return GetClassMemberListUseCase(classRepository)
    }
    
    // Mission Use Cases
    @Provides
    @Singleton
    fun provideGetMissionSpotsUseCase(missionRepository: MissionRepositoryImpl): GetMissionSpotsUseCase {
        return GetMissionSpotsUseCase(missionRepository)
    }
    
    @Provides
    @Singleton
    fun provideGetMissionUseCase(missionRepository: IMissionRepository): GetMissionUseCase {
        return GetMissionUseCase(missionRepository)
    }
    
    @Provides
    @Singleton
    fun provideManageMissionUseCase(missionRepository: IMissionRepository): ManageMissionUseCase {
        return ManageMissionUseCase(missionRepository)
    }
    
    // Settings Use Cases
    @Provides
    @Singleton
    fun provideGetSettingsUseCase(settingsRepository: ISettingsRepository): GetSettingsUseCase {
        return GetSettingsUseCase(settingsRepository)
    }
    
    @Provides
    @Singleton
    fun provideUpdateSettingsUseCase(settingsRepository: ISettingsRepository): UpdateSettingsUseCase {
        return UpdateSettingsUseCase(settingsRepository)
    }
    
    // AR Use Cases
    @Provides
    @Singleton
    fun provideGetAR3DObjectsUseCase(ar3DObjectRepository: AR3DObjectRepository): GetAR3DObjectsUseCase {
        return GetAR3DObjectsUseCase(ar3DObjectRepository)
    }

    // Location Use Cases
    @Provides
    @Singleton
    fun provideGetLocationsUseCase(locationRepository: ILocationRepository): getLocationsUseCase {
        return getLocationsUseCase(locationRepository)
    }

    // CardGame Use Cases
    @Provides
    @Singleton
    fun provideGetBattleHistoryUseCase(cardGameRepository: ICardGameRepository): GetBattleHistoryUseCase {
        return GetBattleHistoryUseCase(cardGameRepository)
    }

    @Provides
    @Singleton
    fun provideGetTeamCardCollectionUseCase(cardGameRepository: ICardGameRepository): GetTeamCardCollectionUseCase {
        return GetTeamCardCollectionUseCase(cardGameRepository)
    }

    @Provides
    @Singleton
    fun provideCreateBattleUseCase(cardGameRepository: ICardGameRepository): CreateBattleUseCase {
        return CreateBattleUseCase(cardGameRepository)
    }

    @Provides
    @Singleton
    fun provideRespondToBattleUseCase(cardGameRepository: ICardGameRepository): RespondToBattleUseCase {
        return RespondToBattleUseCase(cardGameRepository)
    }

    @Provides
    @Singleton
    fun provideGetBattleOpponentsUseCase(cardGameRepository: ICardGameRepository): GetBattleOpponentsUseCase {
        return GetBattleOpponentsUseCase(cardGameRepository)
    }

    @Provides
    @Singleton
    fun provideCancelBattleUseCase(cardGameRepository: ICardGameRepository): CancelBattleUseCase {
        return CancelBattleUseCase(cardGameRepository)
    }

    @Provides
    @Singleton
    fun provideViewBattleResultUseCase(cardGameRepository: ICardGameRepository): ViewBattleResultUseCase {
        return ViewBattleResultUseCase(cardGameRepository)
    }

    @Provides
    @Singleton
    fun provideGetTeamStatsUseCase(cardGameRepository: ICardGameRepository): GetTeamStatsUseCase {
        return GetTeamStatsUseCase(cardGameRepository)
    }

    // Problem Use Cases
    @Provides
    @Singleton
    fun provideGenerateProblemUseCase(problemRepository: IProblemRepository): GenerateProblemUseCase {
        return GenerateProblemUseCase(problemRepository)
    }

    @Provides
    @Singleton
    fun provideRegisterProblemUseCase(problemRepository: IProblemRepository): RegisterProblemUseCase {
        return RegisterProblemUseCase(problemRepository)
    }

    // Spot Use Cases
    @Provides
    @Singleton
    fun provideGetSpotListUseCase(spotRepository: ISpotRepository): GetSpotListUseCase {
        return GetSpotListUseCase(spotRepository)
    }
}