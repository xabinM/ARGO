package com.example.bogoargo.navigation

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bogoargo.ui.screens.ARScreen
import com.example.bogoargo.ui.screens.classRoom.ClassDetailScreen
import com.example.bogoargo.ui.screens.classRoom.ClassManagementScreen
import com.example.bogoargo.ui.screens.classRoom.ClassCreateScreen
import com.example.bogoargo.ui.screens.classRoom.StudentClassDetailScreen
import com.example.bogoargo.ui.screens.GameScreen
import com.example.bogoargo.ui.screens.user.StudentHomeScreen
import com.example.bogoargo.ui.screens.MissionDetailScreen
import com.example.bogoargo.ui.screens.user.LoginScreen
import com.example.bogoargo.ui.screens.user.ProfileScreen
import com.example.bogoargo.ui.screens.user.SignUpScreen
import com.example.bogoargo.ui.screens.SelectHomeScreen
import com.example.bogoargo.ui.screens.SettingsScreen
import com.example.bogoargo.ui.screens.SplashScreen
import com.example.bogoargo.ui.screens.user.TeacherHomeScreen
import com.example.bogoargo.ui.screens.team.TeamCreateScreen
import com.example.bogoargo.ui.screens.team.TeamManagementScreen
import com.example.bogoargo.ui.screens.classRoom.ClassMemberManagementScreen
import com.example.bogoargo.ui.screens.cardgame.CardGameScreen
import com.example.bogoargo.ui.screens.cardgame.CardCollectionScreen
import com.example.bogoargo.ui.screens.cardgame.BattleRequestScreen
import com.example.bogoargo.ui.screens.cardgame.CardSelectionScreen
import com.example.bogoargo.ui.screens.cardgame.BattleResultScreen
import com.example.bogoargo.ui.screens.problem.ClassSelectionForProblemScreen
import com.example.bogoargo.ui.screens.problem.ProblemGenerateScreen
import com.example.bogoargo.ui.screens.classRoom.StudentLocationScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object SignUp : Screen("signUp")
    data object SelectHome : Screen("selectHome")
    data object StudentHome : Screen("studentHome")
    data object Game : Screen("game?classId={classId}&teamId={teamId}&leaderId={leaderId}") {
        fun createRoute(classId: Long = 1L, teamId: Long = 0L, leaderId: Long = 0L) =
            "game?classId=$classId&teamId=$teamId&leaderId=$leaderId"
    }
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object AR : Screen("ar/{spotId}/{latitude}/{longitude}?classId={classId}&teamId={teamId}") {
        fun createRoute(spotId: Long, latitude: Double, longitude: Double, classId: Long = 1L, teamId: Long = 0L) =
            "ar/$spotId/$latitude/$longitude?classId=$classId&teamId=$teamId"
    }

    data object TeacherHome : Screen("teacherHome")
    data object ClassCreate : Screen("classCreate")
    data object ClassManagement : Screen("classManagement")
    data object ClassDetail : Screen("classDetail/{classId}") {
        fun createRoute(classId: Long) = "classDetail/$classId"
    }

    data object StudentClassDetail : Screen("studentClassDetail/{classId}") {
        fun createRoute(classId: Long) = "studentClassDetail/$classId"
    }

    data object TeamCreate : Screen("teamCreate/{classId}") {
        fun createRoute(classId: Long) = "teamCreate/$classId"
    }

    data object TeamManagement : Screen("teamManagement/{classId}") {
        fun createRoute(classId: Long) = "teamManagement/$classId"
    }

    data object ClassMemberManagement : Screen("classMemberManagement/{classId}") {
        fun createRoute(classId: Long) = "classMemberManagement/$classId"
    }
    data object Mission : Screen("mission/{spotId}?classId={classId}&teamId={teamId}") {
        fun createRoute(spotId: Long, classId: Long = 1L, teamId: Long = 0L) =
            "mission/$spotId?classId=$classId&teamId=$teamId"
    }

    data object CardGame : Screen("cardGame/{teamId}/{leaderId}/{classId}") {
        fun createRoute(teamId: Long, leaderId: Long, classId: Long) = "cardGame/$teamId/$leaderId/$classId"
    }

    data object CardCollection : Screen("cardCollection/{teamId}/{classId}") {
        fun createRoute(teamId: Long, classId: Long) = "cardCollection/$teamId/$classId"
    }

    data object BattleRequest : Screen("battleRequest/{teamId}/{leaderId}/{classId}") {
        fun createRoute(teamId: Long, leaderId: Long, classId: Long) = "battleRequest/$teamId/$leaderId/$classId"
    }

    data object CardSelection : Screen("cardSelection/{teamId}/{targetTeamId}/{classId}?matchId={matchId}&isResponse={isResponse}&targetTeamName={targetTeamName}") {
        fun createRoute(
            teamId: Long, 
            targetTeamId: Long,
            classId: Long,
            matchId: Long? = null,
            isResponse: Boolean = false,
            targetTeamName: String = ""
        ) = "cardSelection/$teamId/$targetTeamId/$classId?matchId=${matchId ?: -1}&isResponse=$isResponse&targetTeamName=$targetTeamName"
    }

    data object BattleResult : Screen("battleResult/{myCardId}/{myCardRarity}/{myCardStance}/{opponentCardId}/{opponentCardRarity}/{opponentCardStance}/{isWin}/{myTeamName}/{opponentTeamName}") {
        fun createRoute(
            myCardId: Long,
            myCardRarity: String,
            myCardStance: String,
            opponentCardId: Long,
            opponentCardRarity: String,
            opponentCardStance: String,
            isWin: Boolean,
            myTeamName: String,
            opponentTeamName: String
        ) = "battleResult/$myCardId/$myCardRarity/$myCardStance/$opponentCardId/$opponentCardRarity/$opponentCardStance/$isWin/$myTeamName/$opponentTeamName"
    }

    // Problem generation screens
    data object ClassSelectionForProblemScreen : Screen("classSelectionForProblemScreen")
    data object ProblemGenerate : Screen("problemGenerate/{classId}") {
        fun createRoute(classId: Long) = "problemGenerate/$classId"
    }

    data object StudentLocation : Screen("studentLocation/{classId}") {
        fun createRoute(classId: Long) = "studentLocation/$classId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }
        composable(Screen.SelectHome.route) {
            SelectHomeScreen(navController = navController)
        }
        composable(Screen.StudentHome.route) {
            StudentHomeScreen(navController = navController)
        }
        composable(
            route = Screen.Game.route,
            arguments = listOf(
                navArgument("classId") {
                    type = NavType.LongType
                    defaultValue = 1L
                },
                navArgument("teamId") {
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument("leaderId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getLong("classId") ?: 1L
            val teamId = backStackEntry.arguments?.getLong("teamId") ?: 0L
            val leaderId = backStackEntry.arguments?.getLong("leaderId") ?: 0L
            GameScreen(
                navController = navController,
                classId = classId,
                teamId = teamId,
                leaderId = leaderId
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.TeacherHome.route) {
            TeacherHomeScreen(navController = navController)
        }
        composable(Screen.ClassCreate.route) {
            ClassCreateScreen(navController = navController)
        }
        composable(Screen.ClassManagement.route) {
            ClassManagementScreen(navController = navController)
        }
        composable(
            route = Screen.ClassDetail.route,
            arguments = listOf(
                navArgument("classId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getLong("classId") ?: 0L
            Log.d("DEBUG", "classId = $classId")

            ClassDetailScreen(
                navController = navController,
                classId = classId
            )
        }
        composable(
            route = Screen.StudentClassDetail.route,
            arguments = listOf(
                navArgument("classId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getLong("classId") ?: 0L

            StudentClassDetailScreen(
                navController = navController,
                classId = classId
            )
        }
        composable(
            route = Screen.TeamCreate.route,
            arguments = listOf(navArgument("classId") { type = NavType.LongType })
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getLong("classId") ?: 0L
            TeamCreateScreen(
                navController = navController,
                classId = classId
            )
        }
        composable(
            route = Screen.TeamManagement.route,
            arguments = listOf(navArgument("classId") { type = NavType.LongType })
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getLong("classId") ?: 0L
            TeamManagementScreen(
                navController = navController,
                classId = classId
            )
        }
        composable(
            route = Screen.ClassMemberManagement.route,
            arguments = listOf(navArgument("classId") { type = NavType.LongType })
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getLong("classId") ?: 0L
            ClassMemberManagementScreen(
                navController = navController,
                classId = classId
            )
        }
        composable(
            route = Screen.Mission.route,
            arguments = listOf(
                navArgument("spotId") { type = NavType.LongType },
                navArgument("classId") {
                    type = NavType.LongType
                    defaultValue = 1L
                },
                navArgument("teamId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val spotId = backStackEntry.arguments?.getLong("spotId") ?: 0L
            val classId = backStackEntry.arguments?.getLong("classId") ?: 1L
            val teamId = backStackEntry.arguments?.getLong("teamId") ?: 0L
            MissionDetailScreen(
                spotId = spotId,
                classId = classId,
                teamId = teamId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGame = {
                    navController.navigate(Screen.Game.createRoute(classId, teamId)) {
                        popUpTo(Screen.Game.route) { inclusive = false }
                    }
                }
            )
        }
        composable(
            route = Screen.AR.route,
            arguments = listOf(
                navArgument("spotId") { type = NavType.LongType },
                navArgument("latitude") { type = NavType.FloatType },
                navArgument("longitude") { type = NavType.FloatType },
                navArgument("classId") {
                    type = NavType.LongType
                    defaultValue = 1L
                },
                navArgument("teamId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val spotId = backStackEntry.arguments?.getLong("spotId") ?: 0L
            val latitude = backStackEntry.arguments?.getFloat("latitude")?.toDouble() ?: 0.0
            val longitude = backStackEntry.arguments?.getFloat("longitude")?.toDouble() ?: 0.0
            val classId = backStackEntry.arguments?.getLong("classId") ?: 1L
            val teamId = backStackEntry.arguments?.getLong("teamId") ?: 0L

            ARScreen(
                spotId = spotId,
                latitude = latitude,
                longitude = longitude,
                classId = classId,
                teamId = teamId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMission = { missionSpotId ->
                    navController.navigate(Screen.Mission.createRoute(missionSpotId, classId, teamId))
                }
            )
        }
        composable(
            route = Screen.CardGame.route,
            arguments = listOf(
                navArgument("teamId") { type = NavType.LongType },
                navArgument("leaderId") { type = NavType.LongType },
                navArgument("classId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getLong("teamId") ?: 0L
            val leaderId = backStackEntry.arguments?.getLong("leaderId") ?: 0L
            val classId = backStackEntry.arguments?.getLong("classId") ?: 0L
            CardGameScreen(
                navController = navController,
                teamId = teamId,
                leaderId = leaderId,
                classId = classId
            )
        }
        composable(
            route = Screen.CardCollection.route,
            arguments = listOf(
                navArgument("teamId") { type = NavType.LongType },
                navArgument("classId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getLong("teamId") ?: 0L
            val classId = backStackEntry.arguments?.getLong("classId") ?: 0L
            CardCollectionScreen(
                navController = navController,
                teamId = teamId,
                classId = classId
            )
        }
        composable(
            route = Screen.BattleRequest.route,
            arguments = listOf(
                navArgument("teamId") { type = NavType.LongType },
                navArgument("leaderId") { type = NavType.LongType },
                navArgument("classId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getLong("teamId") ?: 0L
            val leaderId = backStackEntry.arguments?.getLong("leaderId") ?: 0L
            val classId = backStackEntry.arguments?.getLong("classId") ?: 0L
            BattleRequestScreen(
                navController = navController,
                teamId = teamId,
                leaderId = leaderId,
                classId = classId
            )
        }
        composable(
            route = Screen.CardSelection.route,
            arguments = listOf(
                navArgument("teamId") { type = NavType.LongType },
                navArgument("targetTeamId") { type = NavType.LongType },
                navArgument("classId") { type = NavType.LongType },
                navArgument("matchId") { 
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument("isResponse") { 
                    type = NavType.BoolType
                    defaultValue = false
                },
                navArgument("targetTeamName") { 
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getLong("teamId") ?: 0L
            val targetTeamId = backStackEntry.arguments?.getLong("targetTeamId") ?: 0L
            val classId = backStackEntry.arguments?.getLong("classId") ?: 0L
            val matchId = backStackEntry.arguments?.getLong("matchId")?.let { 
                if (it == -1L) null else it 
            }
            val isResponse = backStackEntry.arguments?.getBoolean("isResponse") ?: false
            val targetTeamName = backStackEntry.arguments?.getString("targetTeamName") ?: ""
            
            CardSelectionScreen(
                navController = navController,
                params = com.example.bogoargo.ui.screens.cardgame.CardSelectionParams(
                    teamId = teamId,
                    targetTeamId = targetTeamId,
                    classId = classId,
                    matchId = matchId,
                    isResponse = isResponse,
                    targetTeamName = targetTeamName
                )
            )
        }
        composable(
            route = Screen.BattleResult.route,
            arguments = listOf(
                navArgument("myCardId") { type = NavType.LongType },
                navArgument("myCardRarity") { type = NavType.StringType },
                navArgument("myCardStance") { type = NavType.StringType },
                navArgument("opponentCardId") { type = NavType.LongType },
                navArgument("opponentCardRarity") { type = NavType.StringType },
                navArgument("opponentCardStance") { type = NavType.StringType },
                navArgument("isWin") { type = NavType.BoolType },
                navArgument("myTeamName") { type = NavType.StringType },
                navArgument("opponentTeamName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val myCardId = backStackEntry.arguments?.getLong("myCardId") ?: 0L
            val myCardRarity = backStackEntry.arguments?.getString("myCardRarity") ?: "COMMON"
            val myCardStance = backStackEntry.arguments?.getString("myCardStance") ?: "ATTACK"
            val opponentCardId = backStackEntry.arguments?.getLong("opponentCardId") ?: 0L
            val opponentCardRarity = backStackEntry.arguments?.getString("opponentCardRarity") ?: "COMMON"
            val opponentCardStance = backStackEntry.arguments?.getString("opponentCardStance") ?: "DEFENSE"
            val isWin = backStackEntry.arguments?.getBoolean("isWin") ?: false
            val myTeamName = backStackEntry.arguments?.getString("myTeamName") ?: "우리 팀"
            val opponentTeamName = backStackEntry.arguments?.getString("opponentTeamName") ?: "상대 팀"

            val myBattleCard = com.example.bogoargo.domain.model.BattleCard(
                gameCard = com.example.bogoargo.domain.model.GameCard.create(
                    myCardId,
                    com.example.bogoargo.domain.model.CardTier.valueOf(myCardRarity)
                ),
                battleStance = com.example.bogoargo.domain.model.BattleStance.valueOf(myCardStance)
            )
            val opponentBattleCard = com.example.bogoargo.domain.model.BattleCard(
                gameCard = com.example.bogoargo.domain.model.GameCard.create(
                    opponentCardId,
                    com.example.bogoargo.domain.model.CardTier.valueOf(opponentCardRarity)
                ),
                battleStance = com.example.bogoargo.domain.model.BattleStance.valueOf(opponentCardStance)
            )

            BattleResultScreen(
                navController = navController,
                myBattleCard = myBattleCard,
                opponentBattleCard = opponentBattleCard,
                isWin = isWin,
                myTeamName = myTeamName,
                opponentTeamName = opponentTeamName
            )
        }
        
        // Problem generation screens
        composable(Screen.ClassSelectionForProblemScreen.route) {
            ClassSelectionForProblemScreen(navController = navController)
        }
        composable(
            route = Screen.ProblemGenerate.route,
            arguments = listOf(
                navArgument("classId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getLong("classId") ?: 0L
            ProblemGenerateScreen(
                classId = classId,
                navController = navController
            )
        }
        
        // Student Location Screen
        composable(
            route = Screen.StudentLocation.route,
            arguments = listOf(
                navArgument("classId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getLong("classId") ?: 0L
            StudentLocationScreen(
                navController = navController,
                classId = classId
            )
        }
    }
}

