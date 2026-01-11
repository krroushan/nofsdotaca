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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessaging
import com.prashantpizza.nofsdotaca.model.OrderNotification
import com.prashantpizza.nofsdotaca.notification.NotificationHelper
import com.prashantpizza.nofsdotaca.repository.OrderRepository
import com.prashantpizza.nofsdotaca.ui.OrderDialog
import com.prashantpizza.nofsdotaca.ui.theme.NewOrderFullScreenDisplayOverTheAppsCardAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    private val repository = OrderRepository.getInstance()
    private var currentOrder by mutableStateOf<OrderNotification?>(null)
    private var showOrderDialog by mutableStateOf(false)
    private var fcmToken by mutableStateOf<String?>(null)
    private var isBatteryOptimized by mutableStateOf(true)
    private var hasOverlayPermission by mutableStateOf(false)
    
    // Media player and vibrator for foreground notifications
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var currentRingtoneOrderId: String? = null
    
    // Overlay permission result launcher
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkOverlayPermission()
    }
    
    // Notification permission launcher
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            checkFullScreenIntentPermission()
        } else {
            Log.w(TAG, "Notification permission denied")
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
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
        
        // Check battery optimization status
        checkBatteryOptimization()
        
        // Check overlay permission
        checkOverlayPermission()
        
        // Request notification permission
        requestNotificationPermission()
        
        // Get FCM token
        getFCMToken()
        
        // Register broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
            orderReceiver,
            IntentFilter("com.prashantpizza.nofsdotaca.NEW_ORDER")
        )
        
        setContent {
            NewOrderFullScreenDisplayOverTheAppsCardAppTheme {
                MainScreen(
                    fcmToken = fcmToken,
                    isBatteryOptimized = isBatteryOptimized,
                    hasOverlayPermission = hasOverlayPermission,
                    onTestNotification = { testNotification() },
                    onRequestPermissions = { requestNotificationPermission() },
                    onDisableBatteryOptimization = { requestBatteryOptimizationExemption() },
                    onEnableFullScreen = { checkFullScreenIntentPermission() },
                    onRequestOverlayPermission = { requestOverlayPermission() }
                )
                
                // Show order dialog when notification received in foreground
                if (showOrderDialog && currentOrder != null) {
                    OrderDialog(
                        order = currentOrder!!,
                        onAccept = { handleAccept(currentOrder!!) },
                        onReject = { handleReject(currentOrder!!) },
                        onDismiss = { 
                            stopRingtoneAndVibration()
                            showOrderDialog = false 
                        }
                    )
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
        // Recheck battery optimization when returning to app
        checkBatteryOptimization()
        // Recheck overlay permission
        checkOverlayPermission()
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
                    Log.d(TAG, "Notification permission already granted")
                    checkFullScreenIntentPermission()
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
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
    
    private fun handleAccept(order: OrderNotification) {
        stopRingtoneAndVibration()
        lifecycleScope.launch {
            val result = repository.acceptOrder(order)
            result.onSuccess {
                Toast.makeText(this@MainActivity, "Order accepted", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this@MainActivity, "Failed to accept order", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun handleReject(order: OrderNotification) {
        stopRingtoneAndVibration()
        lifecycleScope.launch {
            val result = repository.rejectOrder(order)
            result.onSuccess {
                Toast.makeText(this@MainActivity, "Order rejected", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this@MainActivity, "Failed to reject order", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun MainScreen(
    fcmToken: String?,
    isBatteryOptimized: Boolean,
    hasOverlayPermission: Boolean,
    onTestNotification: () -> Unit,
    onRequestPermissions: () -> Unit,
    onDisableBatteryOptimization: () -> Unit,
    onEnableFullScreen: () -> Unit,
    onRequestOverlayPermission: () -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "FCM Token",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = fcmToken ?: "Loading...",
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Overlay Permission Warning (CRITICAL for automatic full-screen)
            if (!hasOverlayPermission) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🚨 Display Over Other Apps - REQUIRED",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This permission is ESSENTIAL for automatic full-screen notifications (like Swiggy/Zomato/Uber). Without it, you must tap the notification to see orders.",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onRequestOverlayPermission,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Enable Display Over Other Apps")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✅",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                text = "Display Over Other Apps Enabled",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Full-screen notifications will appear automatically",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Battery Optimization Warning
            if (isBatteryOptimized) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "⚠️ Battery Optimization Enabled",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Notifications may be delayed or missed when app is in background. Disable battery optimization for reliable notifications.",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onDisableBatteryOptimization,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Disable Battery Optimization")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✅",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = "Battery optimization disabled - Notifications will work reliably",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Text(
                text = "Test Notifications",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = onTestNotification,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send Test Notification")
            }
            
            Button(
                onClick = onRequestPermissions,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Request Notification Permissions")
            }
            
            // Full-screen intent permission button (Android 14+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                Button(
                    onClick = onEnableFullScreen,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Enable Full-Screen Notifications (Android 14+)")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "How to Test",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Copy the FCM token above\n" +
                                "2. Send a test notification from Firebase Console or your backend\n" +
                                "3. Or use the 'Send Test Notification' button\n\n" +
                                "Notification will show:\n" +
                                "• Full-screen activity when app is in background/killed\n" +
                                "• Dialog when app is in foreground",
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "FCM Message Format",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = """
                        {
                          "to": "YOUR_FCM_TOKEN",
                          "priority": "high",
                          "data": {
                            "type": "new_order",
                            "title": "New Order",
                            "body": "You have a new order!",
                            "orderId": "ORD-12345",
                            "customerName": "John Doe",
                            "items": "2x Pizza, 1x Coke",
                            "amount": "${'$'}25.99",
                            "address": "123 Main St"
                          }
                        }
                        """.trimIndent(),
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
