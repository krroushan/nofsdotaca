package com.serqfix.partner.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.serqfix.partner.ui.screens.main.*
import com.serqfix.partner.ui.theme.Primary

sealed class TabItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Home : TabItem(
        route = "home_tab",
        title = "Home",
        icon = Icons.Default.Home,
        selectedIcon = Icons.Default.Home
    )
    
    object Bookings : TabItem(
        route = "bookings_tab",
        title = "Bookings",
        icon = Icons.Default.DateRange,
        selectedIcon = Icons.Default.DateRange
    )
    
    object Wallet : TabItem(
        route = "wallet_tab",
        title = "Wallet",
        icon = Icons.Default.AccountCircle,
        selectedIcon = Icons.Default.AccountCircle
    )
    
    object Payments : TabItem(
        route = "payments_tab",
        title = "Payments",
        icon = Icons.Default.Info,
        selectedIcon = Icons.Default.Info
    )
    
    object Profile : TabItem(
        route = "profile_tab",
        title = "Profile",
        icon = Icons.Default.Person,
        selectedIcon = Icons.Default.Person
    )
}

@Composable
fun TabNavigator(
    isSalaryBased: Boolean = false,
    navController: androidx.navigation.NavHostController = rememberNavController(),
    userPreferences: com.serqfix.partner.data.local.UserPreferencesDataStore
) {
    val tabs = if (isSalaryBased) {
        listOf(TabItem.Home, TabItem.Bookings, TabItem.Payments, TabItem.Profile)
    } else {
        listOf(TabItem.Home, TabItem.Bookings, TabItem.Wallet, TabItem.Profile)
    }
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (currentDestination?.hierarchy?.any { it.route == tab.route } == true) {
                                    tab.selectedIcon
                                } else {
                                    tab.icon
                                },
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                        onClick = {
                            navController.navigate(tab.route) {
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TabItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(TabItem.Home.route) {
                HomeScreen(
                    navController = navController,
                    userPreferences = userPreferences
                )
            }
            
            composable(TabItem.Bookings.route) {
                val bookingRepository = com.serqfix.partner.data.repository.BookingRepository()
                BookingsScreen(
                    navController = navController,
                    bookingRepository = bookingRepository,
                    userPreferences = userPreferences
                )
            }
            
            composable(TabItem.Wallet.route) {
                WalletScreen(
                    navController = navController,
                    walletViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
                    userPreferences = userPreferences
                )
            }
            
            composable(TabItem.Payments.route) {
                // PaymentScreen(navController = navController, paymentViewModel = androidx.hilt.navigation.compose.hiltViewModel())
            }
            
            composable(TabItem.Profile.route) {
                ProfileScreen(
                    navController = navController,
                    profileViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
                    userPreferences = userPreferences
                )
            }
        }
    }
}
