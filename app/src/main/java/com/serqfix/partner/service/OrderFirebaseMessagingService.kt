package com.serqfix.partner.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.serqfix.partner.data.model.OrderNotification
import com.serqfix.partner.notification.NotificationHelper
import com.serqfix.partner.utils.AppStateTracker

class OrderFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "OrderFCMService"
        private const val NOTIFICATION_TYPE_NEW_ORDER = "new_order"
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        
        // TODO: Send token to your backend server
        // This should be done when user logs in or app starts
        sendTokenToServer(token)
    }
    
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d(TAG, "Message received from: ${message.from}")
        
        // Check if message contains data payload
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")
            handleDataMessage(message.data)
        }
        
        // Check if message contains notification payload
        message.notification?.let {
            Log.d(TAG, "Message notification body: ${it.body}")
        }
    }
    
    private fun handleDataMessage(data: Map<String, String>) {
        val notificationType = data["type"] ?: return
        
        when (notificationType) {
            NOTIFICATION_TYPE_NEW_ORDER -> {
                handleNewOrderNotification(data)
            }
            else -> {
                Log.w(TAG, "Unknown notification type: $notificationType")
            }
        }
    }
    
    private fun handleNewOrderNotification(data: Map<String, String>) {
        try {
            val order = OrderNotification.fromFCMData(data)
            Log.d(TAG, "New order notification: ${order.orderId}")
            Log.d(TAG, "App in foreground: ${AppStateTracker.isAppInForeground}")
            
            if (AppStateTracker.isAppInForeground) {
                // App is in foreground - show dialog inside the app
                Log.d(TAG, "App in foreground - broadcasting for in-app dialog")
                broadcastOrderNotification(order)
            } else {
                // App is in background/killed - show full-screen notification
                Log.d(TAG, "App in background - showing full-screen notification")
                NotificationHelper.showFullScreenNotification(applicationContext, order)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling new order notification", e)
        }
    }
    
    private fun broadcastOrderNotification(order: OrderNotification) {
        // Send local broadcast to MainActivity to show dialog
        val intent = android.content.Intent("com.serqfix.partner.NEW_ORDER")
        intent.putExtra("order", order)
        androidx.localbroadcastmanager.content.LocalBroadcastManager
            .getInstance(applicationContext)
            .sendBroadcast(intent)
    }
    
    private fun sendTokenToServer(token: String) {
        // TODO: Implement sending token to your backend
        // Example:
        // CoroutineScope(Dispatchers.IO).launch {
        //     try {
        //         yourApiService.registerFCMToken(token)
        //     } catch (e: Exception) {
        //         Log.e(TAG, "Error sending token to server", e)
        //     }
        // }
        Log.d(TAG, "TODO: Send token to server: $token")
    }
}
