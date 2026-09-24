package com.gogart.finflow.presentation.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gogart.finflow.R

sealed class Screen(val route: String, val titleResId: Int, val icon: ImageVector) {
    data object Home : Screen("home", R.string.nav_home, Icons.Default.Home)
    data object Accounts : Screen("accounts", R.string.nav_accounts, Icons.Default.AccountBalanceWallet)
    data object Statistics : Screen("statistics", R.string.nav_statistics, Icons.Default.BarChart)
    data object Budgets : Screen("budgets", R.string.budgets_title, Icons.Default.PieChart)
    data object Settings : Screen("settings", R.string.nav_settings, Icons.Default.Settings)
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(Screen.Home, Screen.Accounts, Screen.Statistics, Screen.Budgets, Screen.Settings)
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(imageVector = screen.icon, contentDescription = stringResource(screen.titleResId)) },
                label = { Text(text = stringResource(screen.titleResId)) },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
