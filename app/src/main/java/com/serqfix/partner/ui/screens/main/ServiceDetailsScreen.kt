package com.serqfix.partner.ui.screens.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
import com.serqfix.partner.data.local.UserPreferencesDataStore
import com.serqfix.partner.data.repository.BookingRepository
import com.serqfix.partner.ui.components.ErrorView
import com.serqfix.partner.ui.components.CustomLoading
import com.serqfix.partner.ui.components.service.*
import com.serqfix.partner.ui.theme.PartnerAppTheme
import com.serqfix.partner.utils.getCurrentLocation
import com.serqfix.partner.utils.LocationTrackingManager
import com.serqfix.partner.util.SocketManager
import com.serqfix.partner.util.ErrorHandler
import org.json.JSONObject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailsScreen(
    navController: NavController,
    bookingId: String?,
    autoAccept: Boolean = false,
    autoReject: Boolean = false,
    bookingRepository: BookingRepository,
    userPreferences: UserPreferencesDataStore
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var isLoading by remember { mutableStateOf(true) }
    var bookingData by remember { mutableStateOf<com.serqfix.partner.data.api.BookingData?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isAccepted by remember { mutableStateOf(false) }
    var isRejected by remember { mutableStateOf(false) }
    var isOutForService by remember { mutableStateOf(false) }
    var isReachedAtLocation by remember { mutableStateOf(false) }
    var isOtpVerified by remember { mutableStateOf(false) }
    var isCompletedOtpVerified by remember { mutableStateOf(false) }
    var userId by remember { mutableStateOf<String?>(null) }
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    
    // Animation states
    val statusTransition = updateTransition(
        targetState = Triple(isAccepted, isOutForService, isReachedAtLocation),
        label = "statusTransition"
    )
    
    // Fetch user ID
    LaunchedEffect(Unit) {
        userId = userPreferences.userId.first()
    }
    
    // Fetch booking data with error handling
    LaunchedEffect(bookingId) {
        val currentContext = context
        if (bookingId != null) {
            try {
                // Check network before making request
                ErrorHandler.checkNetworkAndThrow(currentContext)
                
                bookingRepository.getBookingById(bookingId)
                    .onSuccess { response ->
                        if (response.success && response.data != null) {
                            bookingData = response.data
                            
                            // Update status flags
                            isAccepted = bookingData?.acceptedByServiceProvider == true
                            isOutForService = bookingData?.outForService == true
                            isReachedAtLocation = bookingData?.reachedAtLocation == true
                            isOtpVerified = bookingData?.otpVerified == true
                            isCompletedOtpVerified = bookingData?.completed == true
                            
                            // Handle auto-accept/reject
                            if (autoAccept && !isAccepted && userId != null) {
                                bookingRepository.acceptBooking(bookingId, userId!!)
                                    .onSuccess { 
                                        isAccepted = true
                                        bookingData = bookingData?.copy(acceptedByServiceProvider = true)
                                    }
                                    .onFailure { e ->
                                        ErrorHandler.handleError(e, currentContext, snackbarHostState, scope)
                                    }
                            } else if (autoReject && !isRejected && userId != null) {
                                bookingRepository.rejectBooking(bookingId, userId!!)
                                    .onSuccess { 
                                        isRejected = true
                                        navController.popBackStack()
                                    }
                                    .onFailure { e ->
                                        ErrorHandler.handleError(e, currentContext, snackbarHostState, scope)
                                    }
                            }
                        } else {
                            error = response.message ?: "Failed to fetch booking"
                        }
                        isLoading = false
                    }
                    .onFailure { e ->
                        val appError = ErrorHandler.handleError(e, currentContext, snackbarHostState, scope)
                        error = appError.userFriendlyMessage
                        isLoading = false
                    }
            } catch (e: Exception) {
                val appError = ErrorHandler.handleError(e, currentContext, snackbarHostState, scope)
                error = appError.userFriendlyMessage
                isLoading = false
            }
        } else {
            error = "Booking ID is required"
            isLoading = false
        }
    }
    
    // Get user location when accepted
    LaunchedEffect(isAccepted) {
        if (isAccepted && userLocation == null) {
            scope.launch {
                try {
                    val location = getCurrentLocation(context)
                    userLocation = Pair(location.latitude, location.longitude)
                } catch (e: Exception) {
                    // Location not available, continue without it
                }
            }
        }
    }
    
    // Socket.IO integration for real-time updates
    DisposableEffect(bookingId, userId, isAccepted) {
        var unsubscribe: (() -> Unit)? = null
        
        if (bookingId != null && userId != null && isAccepted) {
            val socket = SocketManager.getSocket() ?: SocketManager.initializeSocket()
            
            if (socket != null && SocketManager.isConnected()) {
                // Join user and booking rooms
                SocketManager.joinRoom("user-$userId")
                SocketManager.joinRoom("booking-$bookingId")
                
                // Subscribe to booking updates
                unsubscribe = SocketManager.subscribeToCollection("bookings") { data ->
                    try {
                        // Check if update is for current booking
                        val documentId = data.optString("documentId", "")
                        val bookingIdInData = data.optString("bookingId", "")
                        val fullDocument = data.optJSONObject("fullDocument")
                        
                        val isRelevantUpdate = documentId == bookingId ||
                                bookingIdInData == bookingId ||
                                fullDocument?.optString("_id") == bookingId ||
                                fullDocument?.optString("bookingId") == bookingId
                        
                        if (isRelevantUpdate) {
                            // Refresh booking data
                            scope.launch {
                                bookingRepository.getBookingById(bookingId!!)
                                    .onSuccess { response ->
                                        if (response.success && response.data != null) {
                                            bookingData = response.data
                                            isOutForService = response.data.outForService == true
                                            isReachedAtLocation = response.data.reachedAtLocation == true
                                            isOtpVerified = response.data.otpVerified == true
                                            isCompletedOtpVerified = response.data.completed == true
                                        }
                                    }
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ServiceDetailsScreen", "Error processing socket update", e)
                    }
                }
            }
        }
        
        onDispose {
            unsubscribe?.invoke()
        }
    }
    
    // Location tracking integration
    DisposableEffect(isOutForService, isReachedAtLocation, bookingId, userId) {
        val shouldTrack = (isOutForService || isReachedAtLocation) && 
                         !isCompletedOtpVerified && 
                         bookingId != null && 
                         userId != null
        
        if (shouldTrack) {
            // Start location tracking
            LocationTrackingManager.startTracking(
                context = context,
                bookingId = bookingId!!,
                providerId = userId!!,
                databaseName = null // TODO: Get from user preferences
            )
        }
        
        onDispose {
            if (shouldTrack) {
                LocationTrackingManager.stopTracking(context)
            }
        }
    }
    
    PartnerAppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Service Details") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = isLoading,
                transitionSpec = {
                    fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically()
                },
                label = "loadingTransition"
            ) { loading ->
                when {
                    loading -> {
                        CustomLoading(
                            loadingText = "Loading service details...",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }
                    error != null -> {
                        ErrorView(
                            message = error ?: "Unknown error",
                            onRetry = {
                                error = null
                                isLoading = true
                                // Retry fetching
                                scope.launch {
                                    if (bookingId != null) {
                                        bookingRepository.getBookingById(bookingId!!)
                                            .onSuccess { response ->
                                                if (response.success && response.data != null) {
                                                    bookingData = response.data
                                                    isAccepted = bookingData?.acceptedByServiceProvider == true
                                                    isOutForService = bookingData?.outForService == true
                                                    isReachedAtLocation = bookingData?.reachedAtLocation == true
                                                    isOtpVerified = bookingData?.otpVerified == true
                                                    isCompletedOtpVerified = bookingData?.completed == true
                                                    error = null
                                                } else {
                                                    error = response.message ?: "Failed to fetch booking"
                                                }
                                                isLoading = false
                                            }
                                            .onFailure { e ->
                                                val appError = ErrorHandler.handleError(e, context, snackbarHostState, scope)
                                                error = appError.userFriendlyMessage
                                                isLoading = false
                                            }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }
                    else -> {
                        AnimatedContent(
                            targetState = bookingData,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) + 
                                slideInVertically(animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300)) + 
                                slideOutVertically(animationSpec = tween(300))
                            },
                            label = "contentTransition"
                        ) { data ->
                            if (data != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                        // Status Banner
                        StatusBanner(
                            bookingData = bookingData,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Map Location Card
                        bookingData?.location?.let { location ->
                            MapLocationCard2(
                                destinationLat = location.latitude,
                                destinationLng = location.longitude,
                                address = bookingData?.address ?: bookingData?.customerLocation,
                                userLat = userLocation?.first,
                                userLng = userLocation?.second,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } ?: bookingData?.customerLocation?.let {
                            // Fallback to address text if location not available
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Location",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        
                        // Booking Info Card
                        BookingInfoCard(
                            bookingData = bookingData,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Accept/Reject Buttons (only if not accepted)
                        if (!isAccepted && !isRejected && bookingData?.acceptedByServiceProvider != true) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (userId != null && bookingId != null) {
                                            scope.launch {
                                                bookingRepository.rejectBooking(bookingId, userId!!)
                                                    .onSuccess {
                                                        isRejected = true
                                                        navController.popBackStack()
                                                    }
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Reject")
                                }
                                
                                Button(
                                    onClick = {
                                        if (userId != null && bookingId != null) {
                                            scope.launch {
                                                bookingRepository.acceptBooking(bookingId, userId!!)
                                                    .onSuccess { response ->
                                                        if (response.success) {
                                                            isAccepted = true
                                                            bookingData = response.data ?: bookingData?.copy(acceptedByServiceProvider = true) ?: bookingData
                                                        }
                                                    }
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Accept")
                                }
                            }
                        }
                        
                        // Customer Details (only if accepted)
                        if (isAccepted && userId != null) {
                            CustomerDetailsCard(
                                bookingData = bookingData,
                                providerPhoneNumber = userId, // TODO: Get actual phone number from user preferences
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        // Out For Service Button (only if accepted but not out for service and not reached)
                        if (isAccepted && !isOutForService && !isReachedAtLocation && userId != null && bookingId != null) {
                            OutForServiceButton(
                                bookingId = bookingId,
                                providerId = userId!!,
                                displayBookingId = bookingData?.bookingId,
                                bookingRepository = bookingRepository,
                                onStatusUpdated = { updated ->
                                    isOutForService = updated
                                    bookingData = bookingData?.copy(outForService = updated)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        // Reached At Location Button (only if out for service but not reached)
                        if (isAccepted && isOutForService && !isReachedAtLocation && userId != null && bookingId != null) {
                            ReachedAtLocationButton(
                                bookingId = bookingId,
                                providerId = userId!!,
                                bookingRepository = bookingRepository,
                                onStatusUpdated = { updated ->
                                    isReachedAtLocation = updated
                                    bookingData = bookingData?.copy(reachedAtLocation = updated)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        // Reached OTP Card (only if accepted and reached at location)
                        if (isAccepted && isOutForService && isReachedAtLocation && !isOtpVerified && userId != null && bookingId != null) {
                            ReachedOtpCard(
                                bookingId = bookingId,
                                bookingRepository = bookingRepository,
                                isOutForService = isOutForService,
                                isReachedAtLocation = isReachedAtLocation,
                                isOtpVerified = isOtpVerified,
                                onOtpVerified = {
                                    isOtpVerified = true
                                    bookingData = bookingData?.copy(otpVerified = true)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        // Completed OTP Card (only if OTP verified but not completed)
                        if (isAccepted && isOtpVerified && !isCompletedOtpVerified && userId != null && bookingId != null) {
                            CompletedOtpCard(
                                bookingId = bookingId,
                                bookingRepository = bookingRepository,
                                isCompletedOtpVerified = isCompletedOtpVerified,
                                onOtpVerified = {
                                    isCompletedOtpVerified = true
                                    bookingData = bookingData?.copy(completed = true)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        // Status message when waiting for OTP
                        if (isAccepted && isOutForService && isReachedAtLocation && !isOtpVerified) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Text(
                                    text = "Please ask customer for OTP to begin service",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        // Payment Info Card
                        PaymentInfoCard(
                            bookingData = bookingData,
                            isAccepted = isAccepted,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Proof of Work (only if OTP verified)
                        if (isAccepted && isOtpVerified && !isCompletedOtpVerified && userId != null && bookingId != null) {
                            ProofOfWorkImageGrid(
                                bookingId = bookingId,
                                providerId = userId!!,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        // Invoice Card (only if OTP verified)
                        if (isAccepted && isOtpVerified && userId != null && bookingId != null) {
                            InvoiceCard(
                                bookingData = bookingData,
                                onGenerateInvoice = {
                                    // TODO: Navigate to generate invoice screen
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        // Payment Details Card (only if OTP verified)
                        if (isAccepted && isOtpVerified && userId != null) {
                            PaymentDetailsCard(
                                bookingData = bookingData,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                                    // Caution Card
                                    CautionCard(
                                        faqs = emptyList(), // TODO: Fetch FAQs from API
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No booking data available")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
