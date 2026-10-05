package com.habitflow.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.habitflow.app.HabitFlowApp
import com.habitflow.app.ui.screens.auth.LoginScreen
import com.habitflow.app.ui.screens.auth.RegisterScreen
import com.habitflow.app.ui.screens.calendar.CalendarScreen
import com.habitflow.app.ui.screens.settings.SettingsScreen
import com.habitflow.app.ui.screens.stats.StatisticsScreen
import com.habitflow.app.ui.screens.today.TodayScreen
import com.habitflow.app.ui.screens.habits.AddHabitScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val TODAY = "today"
    const val CALENDAR = "calendar"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val ADD_HABIT = "add_habit"
}

@Composable
fun HabitFlowNavHost() {
    val navController = rememberNavController()
    var start by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        start = if (HabitFlowApp.instance.authRepository.isLoggedIn()) Routes.TODAY else Routes.LOGIN
    }

    if (start == null) return

    val bottomRoutes = setOf(Routes.TODAY, Routes.CALENDAR, Routes.STATS, Routes.SETTINGS)
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            if (current in bottomRoutes) {
                NavigationBar {
                    listOf(
                        Triple(Routes.TODAY, "Today", Icons.Default.Today),
                        Triple(Routes.CALENDAR, "Calendar", Icons.Default.CalendarMonth),
                        Triple(Routes.STATS, "Stats", Icons.Default.BarChart),
                        Triple(Routes.SETTINGS, "Settings", Icons.Default.Settings),
                    ).forEach { (route, label, icon) ->
                        NavigationBarItem(
                            selected = current == route,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = start!!,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoggedIn = {
                        navController.navigate(Routes.TODAY) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onRegister = { navController.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegistered = {
                        navController.navigate(Routes.TODAY) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.TODAY) {
                TodayScreen(
                    onAddHabit = { navController.navigate(Routes.ADD_HABIT) }
                )
            }
            composable(Routes.CALENDAR) { CalendarScreen() }
            composable(Routes.STATS) { StatisticsScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onLoggedOut = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.ADD_HABIT) {
                AddHabitScreen(onDone = { navController.popBackStack() })
            }
        }
    }
}
