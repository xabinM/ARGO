package com.example.bogoargo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bogoargo.ui.screens.ARScreen
import com.example.bogoargo.ui.screens.GameScreen
import com.example.bogoargo.ui.screens.HomeScreen
import com.example.bogoargo.ui.screens.LoginScreen
import com.example.bogoargo.ui.screens.MissionDetailScreen
import com.example.bogoargo.ui.screens.ProfileScreen
import com.example.bogoargo.ui.screens.SettingsScreen
import com.example.bogoargo.ui.screens.SplashScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object Game : Screen("game")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object AR : Screen("ar/{spotId}/{latitude}/{longitude}") {
        fun createRoute(spotId: Long, latitude: Double, longitude: Double) = "ar/$spotId/$latitude/$longitude"
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
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMission = { missionSpotId ->
                    navController.navigate(Screen.Mission.createRoute(missionSpotId))
                }
            )
        }
        composable(
            route = Screen.Mission.route,
            arguments = listOf(
                navArgument("spotId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val spotId = backStackEntry.arguments?.getLong("spotId") ?: 0L
            MissionDetailScreen(
                spotId = spotId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}