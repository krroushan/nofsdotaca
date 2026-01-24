package com.serqfix.partner.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.serqfix.partner.data.local.UserPreferencesDataStore
import com.serqfix.partner.ui.navigation.NavRoute
import com.serqfix.partner.ui.theme.PartnerAppTheme
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    navController: NavController,
    userPreferences: UserPreferencesDataStore
) {
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        checkLoginStatus(navController, userPreferences) {
            isLoading = false
        }
    }
    
    PartnerAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Navigation will handle the next screen
            }
        }
    }
}

suspend fun checkLoginStatus(
    navController: NavController,
    userPreferences: UserPreferencesDataStore,
    onComplete: () -> Unit
) {
    try {
        // Check if permissions have been granted
        val permissionsCompleted = userPreferences.permissionsCompleted.first()
        
        if (permissionsCompleted) {
            // Check login status
            val isLoggedIn = userPreferences.isLoggedIn.first()
            val userDataJson = userPreferences.userData.first()
            
            if (userDataJson == null) {
                // No stored user data, go to OnBoarding
                navController.navigate(NavRoute.OnBoarding.route) {
                    popUpTo(NavRoute.Splash.route) { inclusive = true }
                }
                onComplete()
                return
            }
            
            // Parse user data (simplified - in real implementation, use proper JSON parsing)
            // val userData = Gson().fromJson(userDataJson, UserData::class.java)
            
            if (isLoggedIn) {
                // User is logged in - navigate to main app
                navController.navigate(NavRoute.TabNavigator.route) {
                    popUpTo(NavRoute.Splash.route) { inclusive = true }
                }
            } else {
                // Not logged in, go to OnBoarding
                navController.navigate(NavRoute.OnBoarding.route) {
                    popUpTo(NavRoute.Splash.route) { inclusive = true }
                }
            }
        } else {
            // Permissions not completed, go to Permissions screen
            navController.navigate(NavRoute.Permissions.route) {
                popUpTo(NavRoute.Splash.route) { inclusive = true }
            }
        }
    } catch (e: Exception) {
        // On error, go to OnBoarding
        navController.navigate(NavRoute.OnBoarding.route) {
            popUpTo(NavRoute.Splash.route) { inclusive = true }
        }
    }
    
    onComplete()
}
