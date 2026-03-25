package com.derrochador.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.derrochador.ui.screen.*
import com.derrochador.ui.viewmodel.DashboardViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Inicio", Icons.Default.Home)
    object Transactions : Screen("transactions", "Gastos", Icons.Default.List)
    object Settings : Screen("settings", "Config", Icons.Default.Settings)
    object AddTransaction : Screen("add_transaction", "Agregar", Icons.Default.Home)
}

private val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Transactions,
    Screen.Settings
)

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val viewModel: DashboardViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val showBottomBar by remember(currentDestination) {
        derivedStateOf { bottomNavItems.any { it.route == currentDestination?.route } }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { _ ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    uiState = uiState,
                    onAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                    onRefresh = { viewModel.refreshSummary() },
                    onDeleteTransaction = { viewModel.deleteTransaction(it) }
                )
            }
            composable(Screen.Transactions.route) {
                TransactionListScreen(
                    transactions = uiState.transactions,
                    onBack = { navController.popBackStack() },
                    onDelete = { viewModel.deleteTransaction(it) }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    isModelReady = uiState.isModelReady,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AddTransaction.route) {
                AddTransactionScreen(
                    onBack = { navController.popBackStack() },
                    onSave = { amount, description, category ->
                        viewModel.saveManualTransaction(amount, description, category)
                    }
                )
            }
        }
    }
}
