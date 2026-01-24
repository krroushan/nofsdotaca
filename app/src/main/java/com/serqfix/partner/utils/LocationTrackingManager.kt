package com.serqfix.partner.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import com.serqfix.partner.service.LocationTrackingService

object LocationTrackingManager {
    
    fun startTracking(
        context: Context,
        bookingId: String,
        providerId: String,
        databaseName: String? = null
    ) {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            putExtra("bookingId", bookingId)
            putExtra("providerId", providerId)
            databaseName?.let { putExtra("databaseName", it) }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
    
    fun stopTracking(context: Context) {
        val intent = Intent(context, LocationTrackingService::class.java)
        context.stopService(intent)
        
        // Clear persisted data
        val prefs = context.getSharedPreferences("LocationTrackingPrefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        
        LocationTrackingService.isTracking = false
    }
    
    fun isTrackingActive(): Boolean {
        return LocationTrackingService.isTracking
    }
}
