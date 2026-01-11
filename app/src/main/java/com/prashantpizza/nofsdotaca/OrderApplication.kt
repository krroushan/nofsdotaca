package com.prashantpizza.nofsdotaca

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.prashantpizza.nofsdotaca.notification.NotificationHelper
import com.prashantpizza.nofsdotaca.utils.AppStateTracker

class OrderApplication : Application() {
    
    companion object {
        private const val TAG = "OrderApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "Application starting...")
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Register activity lifecycle callbacks for app state tracking
        registerActivityLifecycleCallbacks(AppStateTracker)
        
        // Create notification channel
        NotificationHelper.createNotificationChannel(this)
        
        Log.d(TAG, "Application initialized successfully")
    }
}
