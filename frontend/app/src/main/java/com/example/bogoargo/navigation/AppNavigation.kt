package com.example.bogoargo.navigation

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
import com.example.bogoargo.ui.screens.GameScreen
import com.example.bogoargo.ui.screens.HomeScreen
import com.example.bogoargo.ui.screens.MissionDetailScreen
import com.example.bogoargo.ui.screens.user.LoginScreen
import com.example.bogoargo.ui.screens.user.ProfileScreen
import com.example.bogoargo.ui.screens.user.SignUpScreen
import com.example.bogoargo.ui.screens.SettingsScreen
import com.example.bogoargo.ui.screens.SplashScreen
import com.example.bogoargo.ui.screens.user.TeacherMainScreen
import com.example.bogoargo.ui.screens.team.TeamCreateScreen
import com.example.bogoargo.ui.screens.team.TeamManagementScreen
import com.example.bogoargo.ui.screens.classRoom.ClassMemberManagementScreen
import com.example.bogoargo.ui.screens.cardgame.CardGameScreen
import com.example.bogoargo.ui.screens.cardgame.CardCollectionScreen
import com.example.bogoargo.ui.screens.cardgame.BattleRequestScreen
import com.example.bogoargo.ui.screens.cardgame.CardSelectionScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object SignUp : Screen("signUp")
    data object Home : Screen("home")
    data object Game : Screen("game")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object AR : Screen("ar/{spotId}/{latitude}/{longitude}") {
        fun createRoute(spotId: Long, latitude: Double, longitude: Double) =
            "ar/$spotId/$latitude/$longitude"
    }

    data object TeacherMain : Screen("teacherMain")
    data object ClassCreate : Screen("classCreate")
    data object ClassManagement : Screen("classManagement")
    data object ClassDetail :
        Screen("classDetail/{classId}/{schoolName}/{className}/{description}/{region}/{invitationCode}") {
        fun createRoute(
            classId: String,
            schoolName: String,
            className: String,
            description: String,
            region: String,
            invitationCode: String
        ) =
            "classDetail/$classId/$schoolName/$className/$description/$region/$invitationCode"
    }

    data object TeamCreate : Screen("teamCreate/{classId}") {
        fun createRoute(classId: String) = "teamCreate/$classId"
    }

    data object TeamManagement : Screen("teamManagement/{classId}") {
        fun createRoute(classId: String) = "teamManagement/$classId"
    }

    data object ClassMemberManagement : Screen("classMemberManagement/{classId}") {
        fun createRoute(classId: String) = "classMemberManagement/$classId"
    }
    data object Mission : Screen("mission/{spotId}") {
        fun createRoute(spotId: Long) = "mission/$spotId"
    }

    data object CardGame : Screen("cardGame/{teamId}/{leaderId}") {
        fun createRoute(teamId: Long, leaderId: Long) = "cardGame/$teamId/$leaderId"
    }

    data object CardCollection : Screen("cardCollection/{teamId}") {
        fun createRoute(teamId: Long) = "cardCollection/$teamId"
    }

    data object BattleRequest : Screen("battleRequest/{teamId}/{leaderId}") {
        fun createRoute(teamId: Long, leaderId: Long) = "battleRequest/$teamId/$leaderId"
    }

    data object CardSelection : Screen("cardSelection/{teamId}/{targetTeamId}") {
        fun createRoute(teamId: Long, targetTeamId: Long) = "cardSelection/$teamId/$targetTeamId"
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
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Game.route) {
            GameScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.TeacherMain.route) {
            TeacherMainScreen(navController = navController)
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
                navArgument("classId") { type = NavType.StringType },
                navArgument("schoolName") { type = NavType.StringType },
                navArgument("className") { type = NavType.StringType },
                navArgument("description") { type = NavType.StringType },
                navArgument("region") { type = NavType.StringType },
                navArgument("invitationCode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId") ?: ""
            val schoolName = backStackEntry.arguments?.getString("schoolName") ?: ""
            val className = backStackEntry.arguments?.getString("className") ?: ""
            val description = backStackEntry.arguments?.getString("description") ?: ""
            val region = backStackEntry.arguments?.getString("region") ?: ""
            val invitationCode = backStackEntry.arguments?.getString("invitationCode") ?: ""

            ClassDetailScreen(
                navController = navController,
                classId = classId,
                /* schoolName = schoolName,
                 className = className,
                 description = description,
                 region = region,
                 invitationCode = invitationCode */
            )
        }
        composable(
            route = Screen.TeamCreate.route,
            arguments = listOf(navArgument("classId") { type = NavType.StringType })
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId") ?: ""
            TeamCreateScreen(
                navController = navController,
                classId = classId
            )
        }
        composable(
            route = Screen.TeamManagement.route,
            arguments = listOf(navArgument("classId") { type = NavType.StringType })
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId") ?: ""
            TeamManagementScreen(
                navController = navController,
                classId = classId
            )
        }
        composable(
            route = Screen.ClassMemberManagement.route,
            arguments = listOf(navArgument("classId") { type = NavType.StringType })
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId") ?: ""
            ClassMemberManagementScreen(
                navController = navController,
                classId = classId
            )
        }
        composable(
            route = Screen.Mission.route,
            arguments = listOf(navArgument("spotId") { type = NavType.LongType })
        ) { backStackEntry ->
            val spotId = backStackEntry.arguments?.getLong("spotId") ?: 0L
            MissionDetailScreen(
                spotId = spotId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.AR.route,
            arguments = listOf(
                navArgument("spotId") { type = NavType.LongType },
                navArgument("latitude") { type = NavType.FloatType },
                navArgument("longitude") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val spotId = backStackEntry.arguments?.getLong("spotId") ?: 0L
            val latitude = backStackEntry.arguments?.getFloat("latitude")?.toDouble() ?: 0.0
            val longitude = backStackEntry.arguments?.getFloat("longitude")?.toDouble() ?: 0.0
            
            ARScreen(
                spotId = spotId,
                latitude = latitude,
                longitude = longitude,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMission = { missionSpotId ->
                    navController.navigate(Screen.Mission.createRoute(missionSpotId))
                }
            )
        }
        composable(
            route = Screen.CardGame.route,
            arguments = listOf(
                navArgument("teamId") { type = NavType.LongType },
                navArgument("leaderId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getLong("teamId") ?: 0L
            val leaderId = backStackEntry.arguments?.getLong("leaderId") ?: 0L
            CardGameScreen(
                navController = navController,
                teamId = teamId,
                leaderId = leaderId
            )
        }
        composable(
            route = Screen.CardCollection.route,
            arguments = listOf(navArgument("teamId") { type = NavType.LongType })
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getLong("teamId") ?: 0L
            CardCollectionScreen(
                navController = navController,
                teamId = teamId
            )
        }
        composable(
            route = Screen.BattleRequest.route,
            arguments = listOf(
                navArgument("teamId") { type = NavType.LongType },
                navArgument("leaderId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getLong("teamId") ?: 0L
            val leaderId = backStackEntry.arguments?.getLong("leaderId") ?: 0L
            BattleRequestScreen(
                navController = navController,
                teamId = teamId,
                leaderId = leaderId
            )
        }
        composable(
            route = Screen.CardSelection.route,
            arguments = listOf(
                navArgument("teamId") { type = NavType.LongType },
                navArgument("targetTeamId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getLong("teamId") ?: 0L
            val targetTeamId = backStackEntry.arguments?.getLong("targetTeamId") ?: 0L
            CardSelectionScreen(
                navController = navController,
                teamId = teamId,
                targetTeamId = targetTeamId
            )
        }
    }
}

