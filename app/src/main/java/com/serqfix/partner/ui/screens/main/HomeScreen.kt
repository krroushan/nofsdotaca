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
import com.serqfix.partner.ui.viewmodel.BookingViewModel
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    bookingViewModel: BookingViewModel = hiltViewModel(),
    userPreferences: UserPreferencesDataStore
) {
    var isLoading by remember { mutableStateOf(true) }
    var bookings by remember { mutableStateOf<List<com.serqfix.partner.data.api.BookingData>>(emptyList()) }
    var currentBooking by remember { mutableStateOf<com.serqfix.partner.data.api.BookingData?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isAvailable by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        val userId = userPreferences.userId.first()
        if (userId != null) {
            bookingViewModel.getBookings(userId, page = 1, limit = 10, status = "ongoing")
        } else {
            error = "User ID not found"
            isLoading = false
        }
    }
    
    // Observe ViewModel state
    LaunchedEffect(bookingViewModel.uiState) {
        val state = bookingViewModel.uiState.value
        isLoading = state.isLoading
        bookings = state.bookings
        currentBooking = state.currentBooking
        error = state.error
    }
    
    PartnerAppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Home") }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Availability Toggle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isAvailable) "Available" else "Not Available",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Toggle your availability status",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isAvailable,
                            onCheckedChange = { isAvailable = it }
                        )
                    }
                }
                
                // Current Booking
                if (currentBooking != null) {
                    CurrentBookingCard(
                        booking = currentBooking!!,
                        onViewDetails = {
                            navController.navigate("${NavRoute.ServiceDetails.route}?bookingId=${currentBooking!!._id ?: currentBooking!!.bookingId}")
                        }
                    )
                }
                
                // Bookings List
                Text(
                    text = "Ongoing Bookings",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                if (bookings.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No ongoing bookings",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    bookings.forEach { booking ->
                        BookingCard(
                            booking = booking,
                            onClick = {
                                navController.navigate("${NavRoute.ServiceDetails.route}?bookingId=${booking._id ?: booking.bookingId}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentBookingCard(
    booking: com.serqfix.partner.data.api.BookingData,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Booking",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                TextButton(onClick = onViewDetails) {
                    Text("View Details")
                }
            }
            
            Text(
                text = booking.serviceName ?: "Service",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = booking.customerName ?: "Customer",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "₹${booking.amount ?: 0}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun BookingCard(
    booking: com.serqfix.partner.data.api.BookingData,
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = booking.serviceName ?: "Service",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
                Text(
                    text = booking.customerName ?: "Customer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "₹${booking.amount ?: 0}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
