package com.serqfix.partner.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.serqfix.partner.R
import com.serqfix.partner.data.model.OrderNotification
import com.serqfix.partner.ui.OrderNotificationActivity

/**
 * Foreground service to launch OrderNotificationActivity from background.
 * This bypasses Android 10+ background activity launch restrictions.
 */
class OrderLauncherService : Service() {

    companion object {
        private const val TAG = "OrderLauncherService"
        private const val CHANNEL_ID = "order_launcher_service"
        private const val NOTIFICATION_ID = 9999
        const val EXTRA_ORDER = "extra_order"

        fun start(context: Context, order: OrderNotification) {
            val intent = Intent(context, OrderLauncherService::class.java).apply {
                putExtra(EXTRA_ORDER, order)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "OrderLauncherService created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "OrderLauncherService started")

        // Start as foreground service immediately
        startForeground(NOTIFICATION_ID, createForegroundNotification())

        // Get order from intent
        val order = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(EXTRA_ORDER, OrderNotification::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(EXTRA_ORDER)
        }

        if (order != null) {
            // Check if we have overlay permission (like Swiggy/Zomato/Uber)
            val canDrawOverlays = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(this)
            } else {
                true
            }

            Log.d(TAG, "Can draw overlays: $canDrawOverlays")

            if (canDrawOverlays) {
                // Launch the full-screen activity with overlay flags
                val activityIntent = Intent(this, OrderNotificationActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
                    addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    // This is the key flag that allows launching from foreground service
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        addFlags(0x00800000) // FLAG_ACTIVITY_LAUNCH_ADJACENT - helps bypass restrictions
                    }
                    putExtra("order", order)
                }

                try {
                    Log.d(TAG, "Launching OrderNotificationActivity from foreground service with overlay permission...")
                    startActivity(activityIntent)
                    Log.d(TAG, "✅ Activity launched successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to launch activity", e)
                }
            } else {
                Log.w(TAG, "⚠️ Overlay permission not granted. Cannot launch activity automatically.")
                Log.w(TAG, "User must tap notification or grant 'Display over other apps' permission.")
            }
        } else {
            Log.e(TAG, "❌ Order is null, cannot launch activity")
        }

        // Stop the service after launching the activity
        stopSelf()

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "OrderLauncherService destroyed")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Order Launcher Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Service to launch order notifications"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Processing Order")
            .setContentText("Launching order notification...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
}
