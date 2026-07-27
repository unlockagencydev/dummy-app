package com.dosemate.app.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dosemate.app.DoseMateApp
import com.dosemate.app.ui.cabinet.CabinetScreen
import com.dosemate.app.ui.schedule.ScheduleScreen
import com.dosemate.app.ui.settings.SettingsScreen
import com.dosemate.app.ui.theme.BackgroundLight
import com.dosemate.app.ui.theme.NavInactive
import com.dosemate.app.ui.theme.TealPrimary
import com.dosemate.app.viewmodel.CabinetViewModel
import com.dosemate.app.viewmodel.ScheduleViewModel
import com.dosemate.app.viewmodel.SettingsViewModel

sealed class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Schedule : Destination("schedule", "Schedule", Icons.Outlined.CalendarMonth)
    data object Cabinet : Destination("cabinet", "Cabinet", Icons.Outlined.Medication)
    data object Settings : Destination("settings", "Settings", Icons.Outlined.Settings)
}

private val destinations = listOf(
    Destination.Schedule,
    Destination.Cabinet,
    Destination.Settings
)

@Composable
fun DoseMateNavHost(app: DoseMateApp) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val scheduleViewModel: ScheduleViewModel = viewModel(
        factory = ScheduleViewModel.factory(app.repository)
    )
    val cabinetViewModel: CabinetViewModel = viewModel(
        factory = CabinetViewModel.factory(app.repository)
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(app.settingsRepository, app.repository)
    )

    Scaffold(
        containerColor = BackgroundLight,
        // Avoid double status-bar padding: screens handle top insets themselves.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                destinations.forEach { dest ->
                    val selected =
                        currentDestination?.hierarchy?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(dest.icon, contentDescription = dest.label)
                        },
                        label = {
                            Text(
                                text = dest.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = TealPrimary,
                            indicatorColor = TealPrimary,
                            unselectedIconColor = NavInactive,
                            unselectedTextColor = NavInactive
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Schedule.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Destination.Schedule.route) {
                ScheduleScreen(
                    viewModel = scheduleViewModel,
                    onAddClick = {
                        cabinetViewModel.openAddSheet()
                        navController.navigate(Destination.Cabinet.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Destination.Cabinet.route) {
                CabinetScreen(viewModel = cabinetViewModel)
            }
            composable(Destination.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
