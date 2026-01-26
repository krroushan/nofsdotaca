package com.prashantpizza.nofsdotaca

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prashantpizza.nofsdotaca.ui.screens.OrdersScreen
import com.prashantpizza.nofsdotaca.ui.screens.OrderDetailScreen
import com.prashantpizza.nofsdotaca.ui.screens.POSScreen
import com.prashantpizza.nofsdotaca.ui.screens.PaymentsScreen
import com.prashantpizza.nofsdotaca.ui.screens.ProfileScreen
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessaging
import com.prashantpizza.nofsdotaca.model.OrderNotification
import com.prashantpizza.nofsdotaca.notification.NotificationHelper
import com.prashantpizza.nofsdotaca.repository.FcmTokenRepository
import com.prashantpizza.nofsdotaca.repository.OrderRepository
import com.prashantpizza.nofsdotaca.ui.LoginScreen
import com.prashantpizza.nofsdotaca.ui.OrderDialog
import com.prashantpizza.nofsdotaca.ui.screens.PermissionsScreen
import com.prashantpizza.nofsdotaca.ui.theme.NewOrderFullScreenDisplayOverTheAppsCardAppTheme
import com.prashantpizza.nofsdotaca.utils.TokenManager
import com.prashantpizza.nofsdotaca.utils.AuthErrorHandler
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    private lateinit var repository: OrderRepository
    private lateinit var fcmTokenRepository: FcmTokenRepository
    private lateinit var tokenManager: TokenManager
    private lateinit var authErrorHandler: AuthErrorHandler
    private var isAuthenticated by mutableStateOf(false)
    private var currentOrder by mutableStateOf<OrderNotification?>(null)
    private var showOrderDialog by mutableStateOf(false)
    private var fcmToken by mutableStateOf<String?>(null)
    private var isBatteryOptimized by mutableStateOf(true)
    private var hasOverlayPermission by mutableStateOf(false)
    private var hasNotificationPermission by mutableStateOf(false)
    private var showPermissionsScreen by mutableStateOf(false)
    private var pendingOrderId by mutableStateOf<String?>(null) // Order ID from intent
    
    // Media player and vibrator for foreground notifications
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var currentRingtoneOrderId: String? = null
    
    // Overlay permission result launcher
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkOverlayPermission()
        // Update permissions screen state if needed
        if (isAuthenticated) {
            val allPermissionsGranted = !isBatteryOptimized && hasOverlayPermission && hasNotificationPermission
            if (allPermissionsGranted && showPermissionsScreen) {
                // Auto-advance if all permissions are now granted
                showPermissionsScreen = false
            }
        }
    }
    
    // Notification permission launcher
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            checkFullScreenIntentPermission()
        } else {
            Log.w(TAG, "Notification permission denied")
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
        // Update permissions screen state if needed
        if (isAuthenticated) {
            val allPermissionsGranted = !isBatteryOptimized && hasOverlayPermission && hasNotificationPermission
            if (allPermissionsGranted && showPermissionsScreen) {
                // Auto-advance if all permissions are now granted
                showPermissionsScreen = false
            }
        }
    }
    
    // Broadcast receiver for foreground notifications
    private val orderReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val order = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent?.getParcelableExtra("order", OrderNotification::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent?.getParcelableExtra("order")
            }
            
            order?.let {
                Log.d(TAG, "Received order notification in foreground: ${it.orderId}")
                
                // Only process if this is a new order (prevent duplicate broadcasts)
                if (currentRingtoneOrderId != it.orderId) {
                    currentOrder = it
                    showOrderDialog = true
                    currentRingtoneOrderId = it.orderId
                    // Play ringtone and vibrate for foreground notification
                    startRingtoneAndVibration()
                } else {
                    Log.d(TAG, "Ignoring duplicate broadcast for order: ${it.orderId}")
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize TokenManager and Repositories
        tokenManager = TokenManager.getInstance(this)
        repository = OrderRepository.getInstance(this)
        fcmTokenRepository = FcmTokenRepository.getInstance(this)
        authErrorHandler = AuthErrorHandler.getInstance(this)
        
        // Set logout callback for auth error handler
        authErrorHandler.setLogoutCallback {
            // Show session expired message
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show()
            
            // Remove FCM token from backend before logout
            lifecycleScope.launch {
                fcmTokenRepository.removeToken().onFailure {
                    Log.w(TAG, "Failed to remove FCM token on logout: ${it.message}")
                }
            }
            
            // Clear tokens and logout
            tokenManager.clearTokens()
            isAuthenticated = false
            showPermissionsScreen = false
            
            // Unregister receiver
            try {
                LocalBroadcastManager.getInstance(this@MainActivity)
                    .unregisterReceiver(orderReceiver)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering receiver", e)
            }
        }
        
        // Check authentication state
        isAuthenticated = tokenManager.isAuthenticated()
        
        // Handle intent from OrderNotificationActivity (View Details button)
        handleViewOrderDetailsIntent(intent)
        
        // Only initialize app features if authenticated
        if (isAuthenticated) {
            // Check all permissions
            checkBatteryOptimization()
            checkOverlayPermission()
            checkNotificationPermission()
            
            // Check if permissions screen should be shown
            val allPermissionsGranted = !isBatteryOptimized && hasOverlayPermission && hasNotificationPermission
            showPermissionsScreen = !allPermissionsGranted
            
            // Initialize app features
            getFCMToken()
            
            // Register broadcast receiver
            LocalBroadcastManager.getInstance(this).registerReceiver(
                orderReceiver,
                IntentFilter("com.prashantpizza.nofsdotaca.NEW_ORDER")
            )
        }
        
        setContent {
            NewOrderFullScreenDisplayOverTheAppsCardAppTheme {
                when {
                    !isAuthenticated -> {
                        LoginScreen(
                            onLoginSuccess = {
                                // User logged in successfully
                                isAuthenticated = true
                                // Check all permissions
                                checkBatteryOptimization()
                                checkOverlayPermission()
                                checkNotificationPermission()
                                
                                // Check if permissions screen should be shown
                                val allPermissionsGranted = !isBatteryOptimized && hasOverlayPermission && hasNotificationPermission
                                showPermissionsScreen = !allPermissionsGranted
                                
                                // Initialize app features
                                getFCMToken()
                                // Register broadcast receiver
                                LocalBroadcastManager.getInstance(this@MainActivity).registerReceiver(
                                    orderReceiver,
                                    IntentFilter("com.prashantpizza.nofsdotaca.NEW_ORDER")
                                )
                            },
                            tokenManager = tokenManager
                        )
                    }
                    showPermissionsScreen -> {
                        PermissionsScreen(
                            isBatteryOptimized = isBatteryOptimized,
                            hasOverlayPermission = hasOverlayPermission,
                            hasNotificationPermission = hasNotificationPermission,
                            onRequestNotificationPermission = {
                                requestNotificationPermission()
                            },
                            onDisableBatteryOptimization = {
                                requestBatteryOptimizationExemption()
                            },
                            onRequestOverlayPermission = {
                                requestOverlayPermission()
                            },
                            onEnableFullScreen = {
                                checkFullScreenIntentPermission()
                            },
                            onContinue = {
                                // Check permissions again before continuing
                                checkBatteryOptimization()
                                checkOverlayPermission()
                                checkNotificationPermission()
                                showPermissionsScreen = false
                            }
                        )
                    }
                    else -> {
                        // Get pending order ID and clear it
                        val orderIdToView = pendingOrderId
                        if (orderIdToView != null) {
                            pendingOrderId = null // Clear after reading
                        }
                        
                        MainScreen(
                            fcmToken = fcmToken,
                            isBatteryOptimized = isBatteryOptimized,
                            hasOverlayPermission = hasOverlayPermission,
                            initialOrderId = orderIdToView, // Pass pending order ID
                            onTestNotification = { testNotification() },
                            onRequestPermissions = { requestNotificationPermission() },
                            onDisableBatteryOptimization = { requestBatteryOptimizationExemption() },
                            onEnableFullScreen = { checkFullScreenIntentPermission() },
                            onRequestOverlayPermission = { requestOverlayPermission() },
                            onLogout = {
                                // Remove FCM token from backend before logout
                                lifecycleScope.launch {
                                    fcmTokenRepository.removeToken().onFailure {
                                        Log.w(TAG, "Failed to remove FCM token on logout: ${it.message}")
                                    }
                                }
                                // Clear tokens and logout
                                tokenManager.clearTokens()
                                isAuthenticated = false
                                showPermissionsScreen = false
                                // Unregister receiver
                                try {
                                    LocalBroadcastManager.getInstance(this@MainActivity)
                                        .unregisterReceiver(orderReceiver)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error unregistering receiver", e)
                                }
                            }
                        )
                        
                        // Show order dialog when notification received in foreground
                        if (showOrderDialog && currentOrder != null) {
                            OrderDialog(
                                order = currentOrder!!,
                                onDismiss = { 
                                    stopRingtoneAndVibration()
                                    showOrderDialog = false 
                                },
                                onViewDetails = { 
                                    handleViewDetailsFromDialog(currentOrder!!)
                                    stopRingtoneAndVibration()
                                    showOrderDialog = false 
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(orderReceiver)
        stopRingtoneAndVibration()
    }
    
    override fun onResume() {
        super.onResume()
        if (isAuthenticated) {
            // Recheck all permissions when returning to app
            checkBatteryOptimization()
            checkOverlayPermission()
            checkNotificationPermission()
        }
    }
    
    private fun checkNotificationPermission() {
        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission is granted by default on Android < 13
        }
        Log.d(TAG, "Notification permission: $hasNotificationPermission")
    }
    
    private fun checkOverlayPermission() {
        hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
        Log.d(TAG, "Overlay permission: $hasOverlayPermission")
        
        if (!hasOverlayPermission) {
            Log.w(TAG, "⚠️ Overlay permission not granted - automatic full-screen won't work")
        }
    }
    
    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                overlayPermissionLauncher.launch(intent)
            } else {
                Toast.makeText(this, "Overlay permission already granted", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    hasNotificationPermission = true
                    Log.d(TAG, "Notification permission already granted")
                    checkFullScreenIntentPermission()
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            hasNotificationPermission = true
            checkFullScreenIntentPermission()
        }
    }
    
    private fun checkFullScreenIntentPermission() {
        Log.d(TAG, "Checking full screen intent permission...")
        Log.d(TAG, "Android SDK Version: ${Build.VERSION.SDK_INT}")
        Log.d(TAG, "Android Version: ${Build.VERSION.RELEASE}")
        Log.d(TAG, "Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Log.d(TAG, "Android 14+ detected - checking full screen intent permission")
            
            val canUse = NotificationHelper.canUseFullScreenIntent(this)
            Log.d(TAG, "Can use full screen intent: $canUse")
            
            if (!canUse) {
                Log.w(TAG, "Full screen intent permission NOT granted - showing dialog")
                
                // Show explanation dialog
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Full Screen Notification Permission Required")
                    .setMessage("To show incoming orders like a phone call (even when your phone is locked), please allow full screen notifications.\n\nThis ensures you never miss an order!")
                    .setPositiveButton("Open Settings") { _, _ ->
                        try {
                            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                                data = Uri.parse("package:$packageName")
                            }
                            startActivity(intent)
                            Toast.makeText(
                                this,
                                "Please enable 'Full screen notifications' and return to app",
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error opening full screen intent settings", e)
                            Toast.makeText(
                                this,
                                "Please enable full screen notifications manually in Settings → Apps → This App → Notifications",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    .setNegativeButton("Later", null)
                    .setCancelable(false)
                    .show()
            } else {
                Log.d(TAG, "✅ Full screen intent permission already granted")
                Toast.makeText(this, "✅ Full screen notifications enabled", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d(TAG, "Android ${Build.VERSION.SDK_INT} - Full screen intent works automatically (no permission needed)")
            Toast.makeText(
                this, 
                "✅ Full screen notifications supported on Android ${Build.VERSION.RELEASE}", 
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun checkBatteryOptimization() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        isBatteryOptimized = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            !powerManager.isIgnoringBatteryOptimizations(packageName)
        } else {
            false
        }
        
        if (isBatteryOptimized) {
            Log.w(TAG, "App is battery optimized - notifications may be delayed or missed")
        } else {
            Log.d(TAG, "Battery optimization disabled - notifications will work reliably")
        }
    }
    
    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    // Request to ignore battery optimizations
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                    Toast.makeText(
                        this,
                        "Please allow to disable battery optimization for reliable notifications",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Error requesting battery optimization exemption", e)
                    // Fallback to general battery settings
                    try {
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        startActivity(intent)
                    } catch (e2: Exception) {
                        Log.e(TAG, "Error opening battery optimization settings", e2)
                        Toast.makeText(
                            this,
                            "Please disable battery optimization manually in Settings",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                Toast.makeText(this, "Battery optimization already disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }
            
            val token = task.result
            fcmToken = token
            Log.d(TAG, "FCM Token: $token")
            
            // Save FCM token to backend if user is authenticated
            if (isAuthenticated && token != null) {
                lifecycleScope.launch {
                    fcmTokenRepository.saveToken(token, "android").onSuccess {
                        Log.d(TAG, "FCM token saved to backend successfully")
                    }.onFailure {
                        Log.e(TAG, "Failed to save FCM token to backend: ${it.message}")
                    }
                }
            }
        }
    }
    
    private fun testNotification() {
        Log.d(TAG, "=== TEST NOTIFICATION TRIGGERED ===")
        Log.d(TAG, "App in foreground: ${com.prashantpizza.nofsdotaca.utils.AppStateTracker.isAppInForeground}")
        
        val testOrder = OrderNotification.createDummyOrder()
        Log.d(TAG, "Created test order: ${testOrder.orderId}")
        
        NotificationHelper.showFullScreenNotification(this, testOrder)
        
        Toast.makeText(this, "Test notification sent! Check if full-screen appears.", Toast.LENGTH_LONG).show()
        Log.d(TAG, "=== TEST NOTIFICATION COMPLETED ===")
    }
    
    private fun startRingtoneAndVibration() {
        // Stop any existing ringtone first to avoid multiple instances
        stopRingtoneAndVibration()
        
        try {
            // Only start if not already playing
            if (mediaPlayer == null) {
                // Try to use custom ringtone first, fallback to system ringtone
                val customRingtoneResId = resources.getIdentifier("neworder", "raw", packageName)
                
                mediaPlayer = MediaPlayer().apply {
                    if (customRingtoneResId != 0) {
                        // Use custom ringtone from res/raw/order_ringtone.mp3
                        val afd = resources.openRawResourceFd(customRingtoneResId)
                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        afd.close()
                        Log.d(TAG, "Using custom ringtone")
                    } else {
                        // Fallback to system default ringtone
                        val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                        setDataSource(applicationContext, notificationUri)
                        Log.d(TAG, "Using system default ringtone")
                    }
                    isLooping = true
                    setOnErrorListener { mp, what, extra ->
                        Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                        stopRingtoneAndVibration()
                        true
                    }
                    prepare()
                    start()
                }
                Log.d(TAG, "Ringtone started for foreground notification")
            } else {
                Log.d(TAG, "Ringtone already playing, skipping")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing ringtone", e)
            mediaPlayer = null
        }

        // Start vibration only if not already vibrating
        if (vibrator == null) {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            val vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(vibrationPattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(vibrationPattern, 0)
            }
            Log.d(TAG, "Vibration started for foreground notification")
        } else {
            Log.d(TAG, "Vibration already active, skipping")
        }
    }

    private fun stopRingtoneAndVibration() {
        Log.d(TAG, "Stopping ringtone and vibration...")
        
        // Force stop media player
        try {
            mediaPlayer?.let {
                try {
                    if (it.isPlaying) {
                        it.stop()
                        Log.d(TAG, "MediaPlayer stopped")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping MediaPlayer", e)
                }
                
                try {
                    it.release()
                    Log.d(TAG, "MediaPlayer released")
                } catch (e: Exception) {
                    Log.e(TAG, "Error releasing MediaPlayer", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in MediaPlayer cleanup", e)
        } finally {
            mediaPlayer = null
        }
        
        // Force stop vibration
        try {
            vibrator?.let {
                it.cancel()
                Log.d(TAG, "Vibration cancelled")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling vibration", e)
        } finally {
            vibrator = null
        }
        
        // Clear the current order ID
        currentRingtoneOrderId = null
        Log.d(TAG, "Ringtone and vibration cleanup complete")
    }
    
    private fun handleViewDetailsFromDialog(order: OrderNotification) {
        Log.d(TAG, "View details for order from dialog: ${order.orderId}")
        // Use MongoDB ObjectId if available, otherwise use orderNumber
        val orderIdToUse = order.orderMongoId ?: order.orderId
        Log.d(TAG, "Using order ID for navigation: $orderIdToUse (MongoDB ID: ${order.orderMongoId != null})")
        // Set pending order ID to trigger navigation in MainScreen
        pendingOrderId = orderIdToUse
    }
    
    private fun handleViewOrderDetailsIntent(intent: Intent?) {
        val action = intent?.getStringExtra("action")
        val orderId = intent?.getStringExtra("orderId")
        
        if (action == "view_order_details" && orderId != null) {
            Log.d(TAG, "Received view order details intent for order: $orderId")
            // Set pending order ID to trigger navigation in MainScreen
            pendingOrderId = orderId
        }
    }
}

sealed class TabItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Orders : TabItem("Orders", Icons.Default.ListAlt)
    object POS : TabItem("POS", Icons.Default.PointOfSale)
    object Payments : TabItem("Payments", Icons.Default.Payment)
    object Profile : TabItem("Profile", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    fcmToken: String?,
    isBatteryOptimized: Boolean,
    hasOverlayPermission: Boolean,
    initialOrderId: String? = null, // Order ID from intent or dialog
    onTestNotification: () -> Unit,
    onRequestPermissions: () -> Unit,
    onDisableBatteryOptimization: () -> Unit,
    onEnableFullScreen: () -> Unit,
    onRequestOverlayPermission: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf<TabItem>(TabItem.Orders) }
    var selectedOrderId by remember { mutableStateOf<String?>(initialOrderId) }
    
    // Clear initialOrderId after using it
    LaunchedEffect(initialOrderId) {
        if (initialOrderId != null) {
            selectedOrderId = initialOrderId
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedOrderId != null) "Order Details" else selectedTab.title,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (selectedOrderId != null) {
                        IconButton(onClick = { selectedOrderId = null }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            // Only show bottom navigation bar when not viewing order details
            if (selectedOrderId == null) {
                NavigationBar(
                    containerColor = Color.White
                ) {
                    NavigationBarItem(
                        icon = { 
                            TabIcon(
                                icon = TabItem.Orders.icon,
                                isSelected = selectedTab == TabItem.Orders,
                                selectedColor = Color(0xFF2196F3), // Blue
                                unselectedColor = Color(0xFF757575) // Gray
                            )
                        },
                        label = { Text(TabItem.Orders.title) },
                        selected = selectedTab == TabItem.Orders,
                        onClick = { selectedTab = TabItem.Orders }
                    )
                    NavigationBarItem(
                        icon = { 
                            TabIcon(
                                icon = TabItem.POS.icon,
                                isSelected = selectedTab == TabItem.POS,
                                selectedColor = Color(0xFF4CAF50), // Green
                                unselectedColor = Color(0xFF757575) // Gray
                            )
                        },
                        label = { Text(TabItem.POS.title) },
                        selected = selectedTab == TabItem.POS,
                        onClick = { selectedTab = TabItem.POS }
                    )
                    NavigationBarItem(
                        icon = { 
                            TabIcon(
                                icon = TabItem.Payments.icon,
                                isSelected = selectedTab == TabItem.Payments,
                                selectedColor = Color(0xFFFF9800), // Orange
                                unselectedColor = Color(0xFF757575) // Gray
                            )
                        },
                        label = { Text(TabItem.Payments.title) },
                        selected = selectedTab == TabItem.Payments,
                        onClick = { selectedTab = TabItem.Payments }
                    )
                    NavigationBarItem(
                        icon = { 
                            TabIcon(
                                icon = TabItem.Profile.icon,
                                isSelected = selectedTab == TabItem.Profile,
                                selectedColor = Color(0xFF9C27B0), // Purple
                                unselectedColor = Color(0xFF757575) // Gray
                            )
                        },
                        label = { Text(TabItem.Profile.title) },
                        selected = selectedTab == TabItem.Profile,
                        onClick = { selectedTab = TabItem.Profile }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                selectedOrderId != null -> {
                    // Show order detail screen
                    OrderDetailScreen(
                        orderId = selectedOrderId!!,
                        onBack = { selectedOrderId = null }
                    )
                }
                selectedTab == TabItem.Orders -> {
                    OrdersScreen(
                        onOrderClick = { orderId ->
                            selectedOrderId = orderId
                        }
                    )
                }
                selectedTab == TabItem.POS -> POSScreen()
                selectedTab == TabItem.Payments -> PaymentsScreen()
                selectedTab == TabItem.Profile -> ProfileScreen(
                    onLogout = onLogout
                )
            }
        }
    }
    
    // Keep the old settings screen accessible via a separate flow if needed
    // For now, we'll keep the notification settings in a separate composable
    // that can be accessed from Profile screen later
}

@Composable
fun TabIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    selectedColor: Color,
    unselectedColor: Color
) {
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            // Active state: colorful background with icon
            Surface(
                shape = MaterialTheme.shapes.small,
                color = selectedColor.copy(alpha = 0.2f), // Light background color
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = selectedColor, // Bright icon color
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else {
            // Inactive state: gray icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = unselectedColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SettingsScreen(
    fcmToken: String?,
    isBatteryOptimized: Boolean,
    hasOverlayPermission: Boolean,
    onTestNotification: () -> Unit,
    onRequestPermissions: () -> Unit,
    onDisableBatteryOptimization: () -> Unit,
    onEnableFullScreen: () -> Unit,
    onRequestOverlayPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Order Notification System",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        // Settings content can be added here if needed
        Text(
            text = "Settings",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
