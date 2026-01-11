package com.prashantpizza.nofsdotaca.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.prashantpizza.nofsdotaca.R
import com.prashantpizza.nofsdotaca.model.OrderNotification
import com.prashantpizza.nofsdotaca.service.OrderLauncherService
import com.prashantpizza.nofsdotaca.ui.OrderNotificationActivity

object NotificationHelper {
    
    private const val TAG = "NotificationHelper"
    private const val CHANNEL_ID = "order_notifications"
    private const val CHANNEL_NAME = "Order Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications for new orders"
    const val NOTIFICATION_ID = 1001
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Use MAX importance for full-screen intents
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                
                // Set custom sound
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build()
                setSound(soundUri, audioAttributes)
                
                // Show on lockscreen
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                
                // Bypass Do Not Disturb
                setBypassDnd(true)
                
                // Enable lights
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created with HIGH importance")
        }
    }
    
    fun showFullScreenNotification(context: Context, order: OrderNotification) {
        createNotificationChannel(context)
        
        // Create intent for full screen activity with proper flags
        val fullScreenIntent = Intent(context, OrderNotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            putExtra("order", order)
        }
        
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(), // Unique request code
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Content intent (for when notification is tapped)
        val contentIntent = Intent(context, OrderNotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("order", order)
        }
        
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt() + 1,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create notification with full-screen intent
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(order.title)
            .setContentText(order.body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true) // This launches full-screen
            .setContentIntent(contentPendingIntent) // This handles notification tap
            .setAutoCancel(true)
            .setOngoing(false) // Changed to false so it can be dismissed
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Log Android version and permission status
        Log.d(TAG, "Android SDK: ${Build.VERSION.SDK_INT}, Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        
        // Check if we can use full screen intent
        val canUseFullScreen = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val canUse = notificationManager.canUseFullScreenIntent()
            Log.d(TAG, "Android 14+ Full screen intent permission: $canUse")
            if (!canUse) {
                Log.w(TAG, "⚠️ Full screen intent permission NOT granted. User must enable in Settings.")
                Log.w(TAG, "Notification will appear in tray only. Tap to open.")
            }
            canUse
        } else {
            Log.d(TAG, "Android ${Build.VERSION.SDK_INT} - Full screen intent should work automatically")
            true
        }
        
        // Show notification if permission granted
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            Log.d(TAG, "Showing notification with full-screen intent...")
            notificationManager.notify(NOTIFICATION_ID, builder.build())
            Log.d(TAG, "✅ Notification posted successfully")
            
            // Launch activity via foreground service to bypass background restrictions
            Log.d(TAG, "Starting OrderLauncherService to launch full-screen activity...")
            OrderLauncherService.start(context, order)
            Log.d(TAG, "✅ OrderLauncherService started")
        } else {
            Log.w(TAG, "❌ POST_NOTIFICATIONS permission not granted. Cannot show notification.")
        }
    }
    
    fun cancelNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
    
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    
    fun canUseFullScreenIntent(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.canUseFullScreenIntent()
        } else {
            true
        }
    }
}
