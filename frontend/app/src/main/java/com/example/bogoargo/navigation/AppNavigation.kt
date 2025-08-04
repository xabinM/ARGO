package com.example.bogoargo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.material3.*
import com.example.bogoargo.ui.screens.ARScreen
import com.example.bogoargo.ui.screens.ClassDetailScreen
import com.example.bogoargo.ui.screens.ClassManagementScreen
import com.example.bogoargo.ui.screens.ClassCreateScreen
import com.example.bogoargo.ui.screens.GameScreen
import com.example.bogoargo.ui.screens.HomeScreen
import com.example.bogoargo.ui.screens.LoginScreen
import com.example.bogoargo.ui.screens.ProfileScreen
import com.example.bogoargo.ui.screens.SettingsScreen
import com.example.bogoargo.ui.screens.SplashScreen
import com.example.bogoargo.ui.screens.TeacherMainScreen
import com.example.bogoargo.ui.screens.TeamCreateScreen
import com.example.bogoargo.ui.screens.TeamManagementScreen
import com.example.bogoargo.ui.screens.ClassMemberManagementScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
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

        }
    }

