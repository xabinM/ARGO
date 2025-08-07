package com.example.bogoargo.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bogoargo.data.preferences.UserPreferences
import javax.inject.Inject
import com.example.bogoargo.ui.screens.ARScreen
import com.example.bogoargo.ui.screens.classRoom.ClassDetailScreen
import com.example.bogoargo.ui.screens.classRoom.ClassManagementScreen
import com.example.bogoargo.ui.screens.classRoom.ClassCreateScreen
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

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object SignUp : Screen("signUp")
    data object SelectHome : Screen("selectHome")
    data object StudentHome : Screen("studentHome")
    data object Game : Screen("game")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object AR : Screen("ar/{spotId}/{latitude}/{longitude}") {
        fun createRoute(spotId: Long, latitude: Double, longitude: Double) =
            "ar/$spotId/$latitude/$longitude"
    }

    data object TeacherHome : Screen("teacherHome")
    data object ClassCreate : Screen("classCreate")
    data object ClassManagement : Screen("classManagement")
    data object ClassDetail : Screen("classDetail/{classId}") {
        fun createRoute(classId: String) = "classDetail/$classId"
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
        composable(Screen.Game.route) {
            GameScreen(navController = navController)
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
                navArgument("classId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId") ?: ""
            ClassDetailScreen(
                navController = navController,
                classId = classId
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
    }
}

