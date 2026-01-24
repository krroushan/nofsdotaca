package com.serqfix.partner.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.firebase.database.FirebaseDatabase
import com.serqfix.partner.MainActivity

class LocationTrackingService : Service(), LocationListener {
    
    companion object {
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "LocationTrackingChannel"
        private const val CHANNEL_NAME = "Location Tracking Service"
        private const val LOCATION_UPDATE_INTERVAL = 10000L // 10 seconds
        private const val LOCATION_UPDATE_DISTANCE = 30f // 30 meters
        private const val PREFS_NAME = "LocationTrackingPrefs"
        private const val KEY_BOOKING_ID = "bookingId"
        private const val KEY_PROVIDER_ID = "providerId"
        private const val KEY_DATABASE_NAME = "databaseName"
        private const val KEY_IS_TRACKING = "isTracking"
        
        var isTracking = false
    }
    
    private var locationManager: LocationManager? = null
    private var bookingId: String? = null
    private var providerId: String? = null
    private var databaseName: String? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var prefs: SharedPreferences? = null
    private var locationUpdateCount = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        createNotificationChannel()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        // Acquire wake lock
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LocationTrackingService::WakeLock").apply {
            acquire(10 * 60 * 60 * 1000L /*10 hours*/)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        bookingId = intent?.getStringExtra("bookingId") ?: prefs?.getString(KEY_BOOKING_ID, null)
        providerId = intent?.getStringExtra("providerId") ?: prefs?.getString(KEY_PROVIDER_ID, null)
        databaseName = intent?.getStringExtra("databaseName") ?: prefs?.getString(KEY_DATABASE_NAME, null)
        
        if (bookingId != null && providerId != null) {
            prefs?.edit()?.apply {
                putString(KEY_BOOKING_ID, bookingId)
                putString(KEY_PROVIDER_ID, providerId)
                databaseName?.let { putString(KEY_DATABASE_NAME, it) }
                putBoolean(KEY_IS_TRACKING, true)
                apply()
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    createNotification(),
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(NOTIFICATION_ID, createNotification())
            }
            
            startLocationTracking()
            isTracking = true
        } else {
            val wasTracking = prefs?.getBoolean(KEY_IS_TRACKING, false) ?: false
            if (!wasTracking) {
                stopSelf()
            }
        }
        
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationTracking()
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
        prefs?.edit()?.putBoolean(KEY_IS_TRACKING, false)?.apply()
        isTracking = false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tracking your location to share with customers"
                setSound(null, null)
                enableVibration(false)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Tracking Active")
            .setContentText("Sharing your location with customers")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun startLocationTracking() {
        try {
            val hasFineLocation = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
            val hasNetworkLocation = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?: false

            if (!hasFineLocation && !hasNetworkLocation) {
                android.util.Log.e("LocationTrackingService", "Location providers are disabled")
                return
            }

            if (hasFineLocation) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    locationManager?.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        LOCATION_UPDATE_INTERVAL,
                        LOCATION_UPDATE_DISTANCE,
                        this,
                        Looper.getMainLooper()
                    )
                } else {
                    @Suppress("DEPRECATION")
                    locationManager?.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        LOCATION_UPDATE_INTERVAL,
                        LOCATION_UPDATE_DISTANCE,
                        this
                    )
                }
            }

            if (hasNetworkLocation) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    locationManager?.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        LOCATION_UPDATE_INTERVAL,
                        LOCATION_UPDATE_DISTANCE,
                        this,
                        Looper.getMainLooper()
                    )
                } else {
                    @Suppress("DEPRECATION")
                    locationManager?.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        LOCATION_UPDATE_INTERVAL,
                        LOCATION_UPDATE_DISTANCE,
                        this
                    )
                }
            }

            val lastLocation = if (hasFineLocation) {
                locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } else {
                locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }

            lastLocation?.let { onLocationChanged(it) }

        } catch (e: SecurityException) {
            android.util.Log.e("LocationTrackingService", "Location permission denied: ${e.message}", e)
        } catch (e: Exception) {
            android.util.Log.e("LocationTrackingService", "Failed to start location tracking: ${e.message}", e)
        }
    }

    private fun stopLocationTracking() {
        try {
            locationManager?.removeUpdates(this)
        } catch (e: Exception) {
            android.util.Log.e("LocationTrackingService", "Error stopping location tracking", e)
        }
    }

    override fun onLocationChanged(location: Location) {
        try {
            locationUpdateCount++
            val locationData = mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "heading" to (location.bearing.takeIf { it >= 0 } ?: null),
                "speed" to (if (location.hasSpeed()) (location.speed * 3.6).toString() else null),
                "accuracy" to (if (location.hasAccuracy()) location.accuracy else null),
                "timestamp" to System.currentTimeMillis(),
                "providerId" to (providerId ?: ""),
                "updateCount" to locationUpdateCount
            )

            updateFirebaseLocation(locationData)

        } catch (e: Exception) {
            android.util.Log.e("LocationTrackingService", "Error processing location: ${e.message}", e)
        }
    }

    private fun updateFirebaseLocation(locationData: Map<String, Any?>) {
        try {
            if (bookingId == null || providerId == null) return
            
            val firebasePath = if (databaseName != null) {
                "$databaseName/bookings/$bookingId/location"
            } else {
                "bookings/$bookingId/location"
            }
            
            val dbRef = FirebaseDatabase.getInstance().reference.child(firebasePath)
            dbRef.setValue(locationData)
                .addOnSuccessListener {
                    android.util.Log.d("LocationTrackingService", "Location updated to Firebase: $firebasePath")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("LocationTrackingService", "Failed to update Firebase location", e)
                }
        } catch (e: Exception) {
            android.util.Log.e("LocationTrackingService", "Error updating Firebase location", e)
        }
    }

    override fun onProviderEnabled(provider: String) {
        android.util.Log.d("LocationTrackingService", "Provider enabled: $provider")
    }

    override fun onProviderDisabled(provider: String) {
        android.util.Log.d("LocationTrackingService", "Provider disabled: $provider")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        android.util.Log.d("LocationTrackingService", "Status changed: $provider, status: $status")
    }
}
