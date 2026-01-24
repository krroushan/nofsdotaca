package com.serqfix.partner.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.serqfix.partner.data.local.UserPreferencesDataStore
import com.serqfix.partner.ui.navigation.NavRoute
import com.serqfix.partner.ui.theme.PartnerAppTheme
import com.serqfix.partner.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    userPreferences: UserPreferencesDataStore
) {
    var isLoading by remember { mutableStateOf(true) }
    var profile by remember { mutableStateOf<com.serqfix.partner.data.api.ProfileData?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        val userId = userPreferences.userId.first()
        if (userId != null) {
            profileViewModel.getProfile(userId)
        } else {
            error = "User ID not found"
            isLoading = false
        }
    }
    
    // Observe ViewModel state
    LaunchedEffect(profileViewModel.uiState) {
        val state = profileViewModel.uiState.value
        isLoading = state.isLoading
        profile = state.profile
        error = state.error
    }
    
    PartnerAppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Profile") },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate(NavRoute.EditProfile.route)
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                        }
                    }
                )
            }
        ) { innerPadding ->
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                profile != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Profile Header
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Profile Image placeholder
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                Text(
                                    text = profile?.name ?: "Name",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                                
                                Text(
                                    text = profile?.email ?: "Email",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Text(
                                    text = profile?.phoneNumber ?: "Phone",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Profile Details
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Profile Information",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                                
                                ProfileInfoRow("Status", if (profile?.active == true) "Active" else "Inactive")
                                ProfileInfoRow("Verified", if (profile?.isVerified == true) "Yes" else "No")
                                ProfileInfoRow("Available", if (profile?.available == true) "Yes" else "No")
                                if (!profile?.address.isNullOrEmpty()) {
                                    ProfileInfoRow("Address", profile?.address ?: "")
                                }
                            }
                        }
                        
                        // Menu Items
                        MenuItem(
                            title = "Edit Profile",
                            icon = Icons.Default.Edit,
                            onClick = { navController.navigate(NavRoute.EditProfile.route) }
                        )
                        
                        MenuItem(
                            title = "Bookings",
                            icon = Icons.Default.DateRange,
                            onClick = { navController.navigate(NavRoute.Bookings.route) }
                        )
                        
                        MenuItem(
                            title = "Payments",
                            icon = Icons.Default.Info,
                            onClick = { navController.navigate(NavRoute.Payment.route) }
                        )
                        
                        MenuItem(
                            title = "Wallet",
                            icon = Icons.Default.AccountCircle,
                            onClick = { navController.navigate(NavRoute.Wallet.route) }
                        )
                        
                        MenuItem(
                            title = "Tickets",
                            icon = Icons.Default.Info,
                            onClick = { navController.navigate(NavRoute.TicketsList.route) }
                        )
                        
                        MenuItem(
                            title = "Settings",
                            icon = Icons.Default.Settings,
                            onClick = { /* Navigate to settings */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
    }
}

@Composable
fun MenuItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
