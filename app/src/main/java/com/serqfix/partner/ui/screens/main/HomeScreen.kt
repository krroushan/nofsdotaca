package com.serqfix.partner.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.serqfix.partner.data.local.UserPreferencesDataStore
import com.serqfix.partner.data.repository.BookingRepository
import com.serqfix.partner.ui.components.amc.AMCNotificationCard
import com.serqfix.partner.ui.components.attendance.AttendanceCard
import com.serqfix.partner.ui.components.assets.AssetNotificationCard
import com.serqfix.partner.ui.components.CustomLoading
import com.serqfix.partner.ui.components.home.*
import com.serqfix.partner.ui.navigation.NavRoute
import com.serqfix.partner.ui.theme.PartnerAppTheme
import com.serqfix.partner.ui.viewmodel.HomeViewModel
import com.serqfix.partner.utils.*
import com.serqfix.partner.utils.NetworkMonitor
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel(),
    userPreferences: UserPreferencesDataStore
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Network monitoring
    val networkMonitor = remember { NetworkMonitor(context) }
    val isConnected by networkMonitor.isConnected.collectAsState(initial = true)
    
    LaunchedEffect(isConnected) {
        homeViewModel.setNetworkConnected(isConnected)
    }
    
    val scope = rememberCoroutineScope()
    
    PartnerAppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!isConnected) {
                // No Connection View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFECEFF1)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // No internet image placeholder
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "No Internet Connection",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please check your network settings",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp
                        ),
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { homeViewModel.refreshData() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Try Again",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            } else if (uiState.isLoading) {
                // Loading View
                CustomLoading(loadingText = "Fetching Ongoing Bookings...")
            } else {
                // Main Content - Match React Native structure
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    HomepageHeader(
                        userData = uiState.userData,
                        available = uiState.isAvailable,
                        onAvailabilityChange = { homeViewModel.toggleAvailability() },
                        navController = navController,
                        unreadCount = 0, // TODO: Implement notification counter
                        isLoading = uiState.isRefreshing
                    )
                    
                    // Refresh indicator
                    if (uiState.isRefreshing) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Asset Notification Cards (outside scroll, like React Native)
                    if (uiState.assetNotifications.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8F9FA))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.assetNotifications.forEach { asset ->
                                AssetNotificationCard(
                                    asset = asset,
                                    onViewDetails = {
                                        navController.navigate(NavRoute.Profile.route)
                                    },
                                    onDismiss = {
                                        homeViewModel.handleAssetDismiss(asset._id ?: "")
                                    },
                                    navController = navController
                                )
                            }
                        }
                    }
                    
                    // AMC Notification Cards (outside scroll, like React Native)
                    if (uiState.pendingAMCAssignments.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8F9FA))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.pendingAMCAssignments.forEach { assignment ->
                                AMCNotificationCard(
                                    assignment = assignment,
                                    onAccept = {
                                        homeViewModel.handleAMCAccept(assignment._id ?: "")
                                    },
                                    onReject = {
                                        homeViewModel.handleAMCReject(assignment._id ?: "")
                                    },
                                    onViewDetails = {
                                        // TODO: Navigate to AMC order details
                                    }
                                )
                            }
                        }
                    }
                    
                    // Scrollable Content Container (like React Native's scrollViewContainer)
                    // Calculate conditions outside to avoid state reads during measurement
                    val isSalaryOnly = uiState.userData?.provider_payment_type == "salary-only"
                    val hasCurrentBooking = uiState.currentBooking != null
                    val currentBookingData = uiState.currentBooking
                    val scrollState = rememberScrollState()
                    
                    // Container Box for scrollable content and absolutely positioned CurrentOrderCard
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Scrollable Content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                        ) {
                        
                        // Attendance Card (only for salary-only providers on working days)
                        if (isSalaryOnly && uiState.isWorkingDay && !uiState.isHoliday) {
                            AttendanceCard(
                                todayRecord = uiState.todayAttendance,
                                onPunchIn = {
                                    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                        try {
                                            val location = try {
                                                getCurrentLocation(context).toApiLocationData()
                                            } catch (e: Exception) {
                                                null
                                            }
                                            homeViewModel.punchAttendance("login", location)
                                        } catch (e: Exception) {
                                            // Handle error
                                        }
                                    }
                                },
                                onPunchOut = {
                                    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                        try {
                                            val location = try {
                                                getCurrentLocation(context).toApiLocationData()
                                            } catch (e: Exception) {
                                                null
                                            }
                                            homeViewModel.punchAttendance("logout", location)
                                        } catch (e: Exception) {
                                            // Handle error
                                        }
                                    }
                                },
                                onViewDetails = {
                                    navController.navigate(NavRoute.AttendanceDetails.route)
                                },
                                loading = false, // TODO: Add loading state
                                navController = navController
                            )
                        } else if (isSalaryOnly && !uiState.isWorkingDay) {
                            // Not a working day
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color(0xFFFF9800)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Today is not a working day",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        ),
                                        color = Color(0xFF424242)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Enjoy your day off!",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp
                                        ),
                                        color = Color(0xFF757575)
                                    )
                                }
                            }
                        } else if (isSalaryOnly && uiState.isHoliday) {
                            // Holiday
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color(0xFF4CAF50)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Today is a holiday",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        ),
                                        color = Color(0xFF424242)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "No attendance required",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp
                                        ),
                                        color = Color(0xFF757575)
                                    )
                                }
                            }
                        }
                        
                        // OnGoing Services Card
                        OnGoingServicesCard(navController = navController)
                        
                        // Ongoing Bookings List
                        if (uiState.bookings.isEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No Ongoing Bookings found",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 16.sp
                                        ),
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        } else {
                            uiState.bookings.forEach { booking ->
                                ServiceCard(
                                    booking = booking,
                                    navController = navController
                                )
                            }
                        }
                        
                            // Bottom spacing for CurrentOrderCard
                            Spacer(
                                modifier = Modifier.height(
                                    if (hasCurrentBooking) 200.dp else 70.dp
                                )
                            )
                        }
                        
                        // Current Order Card (Fixed Bottom) - Positioned absolutely like React Native
                        // Use AnimatedVisibility to properly handle composition lifecycle
                        if (hasCurrentBooking && currentBookingData != null) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = true,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .zIndex(1000f),
                                enter = androidx.compose.animation.slideInVertically(
                                    initialOffsetY = { it }
                                ) + androidx.compose.animation.fadeIn(),
                                exit = androidx.compose.animation.slideOutVertically(
                                    targetOffsetY = { it }
                                ) + androidx.compose.animation.fadeOut()
                            ) {
                                CurrentOrderCard(
                                    currentBooking = currentBookingData,
                                    serviceProviderId = uiState.userData?.id ?: uiState.userData?._id ?: "",
                                    isAvailable = uiState.isAvailable,
                                    onAccept = { bookingId ->
                                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                            try {
                                                val location = try {
                                                    getCurrentLocation(context).toApiLocationData()
                                                } catch (e: Exception) {
                                                    null
                                                }
                                                // Accept booking via repository
                                                val bookingRepo = BookingRepository()
                                                val userId = uiState.userData?.id ?: uiState.userData?._id ?: ""
                                                bookingRepo.acceptBooking(bookingId, userId, location?.latitude, location?.longitude)
                                                    .onSuccess {
                                                        // Handle booking accepted
                                                        homeViewModel.handleBookingAccepted(bookingId)
                                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                            navController.navigate("${NavRoute.ServiceDetails.route}?bookingId=$bookingId")
                                                        }
                                                    }
                                                    .onFailure {
                                                        // Handle error - show toast or error message
                                                    }
                                            } catch (e: Exception) {
                                                // Handle error
                                            }
                                        }
                                    },
                                    navController = navController
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
