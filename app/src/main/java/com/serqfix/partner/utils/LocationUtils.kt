package com.serqfix.partner.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null
)

suspend fun getCurrentLocation(context: Context): LocationData {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    
    // Check permissions
    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    
    if (!hasPermission) {
        throw SecurityException("Location permission not granted")
    }
    
    return suspendCancellableCoroutine { continuation ->
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMaxUpdateDelayMillis(5000)
            .build()
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    continuation.resume(
                        LocationData(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracy = location.accuracy
                        )
                    )
                } else {
                    continuation.resumeWithException(Exception("Location is null"))
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            context.mainLooper
        )
        
        // Also try to get last known location as fallback
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null && !continuation.isCompleted) {
                continuation.resume(
                    LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy
                    )
                )
            }
        }
        
        continuation.invokeOnCancellation {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}

// Calculate distance between two points using Haversine formula
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371 // Radius of the earth in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return R * c // Distance in km
}

// Estimate travel time based on distance (rough estimate)
fun estimateTravelTime(distanceKm: Double): Int {
    // Assume average speed of 30 km/h in city
    val timeHours = distanceKm / 30.0
    return (timeHours * 60).toInt() // Time in minutes
}
