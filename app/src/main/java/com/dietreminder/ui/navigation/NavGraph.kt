package com.dietreminder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dietreminder.ui.dashboard.DashboardScreen
import com.dietreminder.ui.history.HistoryScreen
import com.dietreminder.ui.medicine.MedicineScreen
import com.dietreminder.ui.settings.SettingsScreen
import com.dietreminder.ui.setup.SetupWizardScreen

sealed class Screen(val route: String) {
    object Setup : Screen("setup")
    object Dashboard : Screen("dashboard")
    object Medicine : Screen("medicine")
    object History : Screen("history")
    object Settings : Screen("settings")
}

@Composable
fun AppNavGraph(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Setup.route) {
            SetupWizardScreen(onSetupComplete = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Setup.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateMedicine = { navController.navigate(Screen.Medicine.route) },
                onNavigateHistory = { navController.navigate(Screen.History.route) },
                onNavigateSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Medicine.route) {
            MedicineScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.History.route) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onResetComplete = {
                    navController.navigate(Screen.Setup.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
