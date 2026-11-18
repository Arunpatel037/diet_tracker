package com.dietreminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.dietreminder.ui.navigation.AppNavGraph
import com.dietreminder.ui.navigation.Screen
import com.dietreminder.ui.setup.SetupViewModel
import com.dietreminder.ui.theme.DietReminderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DietReminderTheme {
                val setupVm: SetupViewModel = hiltViewModel()
                val setupComplete by setupVm.setupComplete.collectAsState()
                if (setupComplete != null) {
                    val navController = rememberNavController()
                    val start = if (setupComplete == true) Screen.Dashboard.route else Screen.Setup.route
                    AppNavGraph(navController = navController, startDestination = start)
                }
            }
        }
    }
}
