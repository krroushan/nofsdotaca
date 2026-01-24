package com.serqfix.partner

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.serqfix.partner.notification.NotificationHelper
import com.serqfix.partner.utils.AppStateTracker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PartnerApplication : Application() {
    
    companion object {
        private const val TAG = "PartnerApplication"
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
