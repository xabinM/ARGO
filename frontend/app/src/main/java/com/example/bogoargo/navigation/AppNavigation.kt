package com.example.bogoargo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bogoargo.ui.screens.ARScreen
import com.example.bogoargo.ui.screens.ClassInfoScreen
import com.example.bogoargo.ui.screens.ClassManagementScreen
import com.example.bogoargo.ui.screens.ClassMemberManagementScreen
import com.example.bogoargo.ui.screens.ClassTeamManagementScreen
import com.example.bogoargo.ui.screens.CreateClassScreen
import com.example.bogoargo.ui.screens.GameScreen
import com.example.bogoargo.ui.screens.HomeScreen
import com.example.bogoargo.ui.screens.LoginScreen
import com.example.bogoargo.ui.screens.ProfileScreen
import com.example.bogoargo.ui.screens.SettingsScreen
import com.example.bogoargo.ui.screens.SplashScreen
import com.example.bogoargo.ui.screens.TeacherMainScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object Game : Screen("game")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object TeacherMain : Screen("teacherMain")
    data object CreateClass : Screen("createClass")
    data object ClassManagement : Screen("classManagement")
    data object ClassInfo : Screen("classInfo/{classId}/{schoolName}/{className}/{description}/{region}/{invitationCode}") {
        fun createRoute(classId: String, schoolName: String, className: String, description: String, region: String, invitationCode: String) = 
            "classInfo/$classId/$schoolName/$className/$description/$region/$invitationCode"
    }
    data object ClassMemberManagement : Screen("classMemberManagement/{classId}") {
        fun createRoute(classId: String) = "classMemberManagement/$classId"
    }
    data object ClassTeamManagement : Screen("classTeamManagement/{classId}") {
        fun createRoute(classId: String) = "classTeamManagement/$classId"
    }
    data object AR : Screen("ar/{spotId}") {
        fun createRoute(spotId: Long) = "ar/$spotId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.TeacherMain.route
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
        composable(Screen.CreateClass.route) {
            CreateClassScreen(navController = navController)
        }
        composable(Screen.ClassManagement.route) {
            ClassManagementScreen(navController = navController)
        }
        composable(
            route = Screen.ClassInfo.route,
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
            
            ClassInfoScreen(
                navController = navController,
                classId = classId,
                schoolName = schoolName,
                className = className,
                description = description,
                region = region,
                invitationCode = invitationCode
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
            route = Screen.ClassTeamManagement.route,
            arguments = listOf(navArgument("classId") { type = NavType.StringType })
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId") ?: ""
            ClassTeamManagementScreen(
                navController = navController,
                classId = classId
            )
        }
        composable(
            route = Screen.AR.route,
            arguments = listOf(navArgument("spotId") { type = NavType.LongType })
        ) { backStackEntry ->
            val spotId = backStackEntry.arguments?.getLong("spotId") ?: 0L
            ARScreen(
                spotId = spotId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}