package com.habitflow.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.habitflow.app.HabitFlowApp
import com.habitflow.app.ui.screens.auth.LoginScreen
import com.habitflow.app.ui.screens.auth.RegisterScreen
import com.habitflow.app.ui.screens.calendar.CalendarScreen
import com.habitflow.app.ui.screens.habits.AddHabitScreen
import com.habitflow.app.ui.screens.habits.HabitsListScreen
import com.habitflow.app.ui.screens.settings.SettingsScreen
import com.habitflow.app.ui.screens.stats.StatisticsScreen
import com.habitflow.app.ui.screens.today.TodayScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val TODAY = "today"
    const val HABITS = "habits"
    const val CALENDAR = "calendar"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val ADD_HABIT = "add_habit"
    const val EDIT_HABIT = "edit_habit/{id}"
    fun editHabit(id: String) = "edit_habit/$id"
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
        containerColor = Color(0xFF0B1326),
        bottomBar = {
            if (current in bottomRoutes) {
                NavigationBar(containerColor = Color(0xFF131B2E)) {
                    listOf(
                        Triple(Routes.TODAY, "Today", Icons.Default.Today),
                        Triple(Routes.CALENDAR, "Calendar", Icons.Default.CalendarMonth),
                        Triple(Routes.STATS, "Statistics", Icons.Default.BarChart),
                        Triple(Routes.SETTINGS, "Settings", Icons.Default.Settings),
                    ).forEach { (route, label, icon) ->
                        NavigationBarItem(
                            selected = current == route,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, label) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFC0C1FF),
                                selectedTextColor = Color(0xFFC0C1FF),
                                indicatorColor = Color(0xFF222A3D),
                                unselectedIconColor = Color(0xFF908FA0),
                                unselectedTextColor = Color(0xFF908FA0)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = start!!, modifier = Modifier.padding(padding)) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoggedIn = { navController.navigate(Routes.TODAY) { popUpTo(Routes.LOGIN) { inclusive = true } } },
                    onRegister = { navController.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegistered = { navController.navigate(Routes.TODAY) { popUpTo(Routes.LOGIN) { inclusive = true } } },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.TODAY) { TodayScreen(onAddHabit = { navController.navigate(Routes.ADD_HABIT) }) }
            composable(Routes.HABITS) {
                HabitsListScreen(
                    onAdd = { navController.navigate(Routes.ADD_HABIT) },
                    onEdit = { id -> navController.navigate(Routes.editHabit(id)) }
                )
            }
            composable(Routes.CALENDAR) { CalendarScreen() }
            composable(Routes.STATS) { StatisticsScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onLoggedOut = { navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } },
                    onManageHabits = { navController.navigate(Routes.HABITS) }
                )
            }
            composable(Routes.ADD_HABIT) { AddHabitScreen(onDone = { navController.popBackStack() }) }
            composable(Routes.EDIT_HABIT, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                AddHabitScreen(habitId = entry.arguments?.getString("id"), onDone = { navController.popBackStack() })
            }
        }
    }
}
