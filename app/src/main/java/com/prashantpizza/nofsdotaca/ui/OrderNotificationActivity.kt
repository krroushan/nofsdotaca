package com.prashantpizza.nofsdotaca.ui

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.prashantpizza.nofsdotaca.model.OrderNotification
import com.prashantpizza.nofsdotaca.notification.NotificationHelper
import com.prashantpizza.nofsdotaca.repository.OrderRepository
import com.prashantpizza.nofsdotaca.ui.theme.NewOrderFullScreenDisplayOverTheAppsCardAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OrderNotificationActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "OrderNotificationActivity"
        private const val AUTO_DISMISS_TIMEOUT = 60_000L // 60 seconds
    }
    
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private lateinit var repository: OrderRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize repository with context
        repository = OrderRepository.getInstance(this)
        
        // Show over lockscreen and turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        
        // Get order data from intent
        val order = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("order", OrderNotification::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("order")
        } ?: OrderNotification.createDummyOrder()
        
        Log.d(TAG, "Showing full screen notification for order: ${order.orderId}")
        
        // Start ringtone and vibration
        startRingtoneAndVibration()
        
        setContent {
            NewOrderFullScreenDisplayOverTheAppsCardAppTheme {
                OrderNotificationScreen(
                    order = order,
                    onDismiss = { dismissNotification() },
                    onViewDetails = { handleViewDetails(order) }
                )
            }
        }
        
        // Auto-dismiss after timeout
        lifecycleScope.launch {
            delay(AUTO_DISMISS_TIMEOUT)
            Log.d(TAG, "Auto-dismissing notification after timeout")
            dismissNotification()
        }
    }
    
    private fun startRingtoneAndVibration() {
        try {
            // Try to use custom ringtone first, fallback to system ringtone
            val customRingtoneResId = resources.getIdentifier("neworder", "raw", packageName)
            
            val notificationUri = if (customRingtoneResId != 0) {
                // Use custom ringtone from res/raw/order_ringtone.mp3
                Uri.parse("android.resource://$packageName/$customRingtoneResId")
            } else {
                // Fallback to system default ringtone
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
            
            ringtone = RingtoneManager.getRingtone(applicationContext, notificationUri)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone?.isLooping = true
                ringtone?.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            }
            
            ringtone?.play()
            Log.d(TAG, "Ringtone started (custom: ${customRingtoneResId != 0})")
            
            // Start vibration
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            
            val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(pattern, 0)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting ringtone/vibration", e)
        }
    }
    
    private fun stopRingtoneAndVibration() {
        try {
            ringtone?.stop()
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping ringtone/vibration", e)
        }
    }
    
    private fun handleViewDetails(order: OrderNotification) {
        Log.d(TAG, "View details for order: ${order.orderId}")
        // Use MongoDB ObjectId if available, otherwise use orderNumber
        val orderIdToUse = order.orderMongoId ?: order.orderId
        Log.d(TAG, "Using order ID for navigation: $orderIdToUse (MongoDB ID: ${order.orderMongoId != null})")
        // Navigate to MainActivity with orderId
        val intent = Intent(this, com.prashantpizza.nofsdotaca.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("orderId", orderIdToUse)
            putExtra("action", "view_order_details")
        }
        startActivity(intent)
        dismissNotification()
    }
    
    private fun dismissNotification() {
        stopRingtoneAndVibration()
        NotificationHelper.cancelNotification(this)
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopRingtoneAndVibration()
    }
}

@Composable
fun OrderNotificationScreen(
    order: OrderNotification,
    onDismiss: () -> Unit,
    onViewDetails: () -> Unit
) {
    // Black transparent overlay background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        // Popup card - similar to foreground dialog
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E1E)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Order icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Order",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Title
                Text(
                    text = order.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Body
                Text(
                    text = order.body,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Order details card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2A2A)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        OrderDetailRow("Order ID", order.orderId)
                        Spacer(modifier = Modifier.height(10.dp))
                        OrderDetailRow("Customer", order.customerName)
                        Spacer(modifier = Modifier.height(10.dp))
                        OrderDetailRow("Items", order.items)
                        Spacer(modifier = Modifier.height(10.dp))
                        OrderDetailRow("Amount", order.amount, highlight = true)
                        Spacer(modifier = Modifier.height(10.dp))
                        OrderDetailRow("Address", order.address)
                        Spacer(modifier = Modifier.height(10.dp))
                        OrderDetailRow("Time", order.getFormattedTime())
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Dismiss button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF757575)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Dismiss",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // View Details button
                    Button(
                        onClick = onViewDetails,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "View Details",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderDetailRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = if (highlight) Color(0xFF4CAF50) else Color.White,
            fontSize = 14.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}
