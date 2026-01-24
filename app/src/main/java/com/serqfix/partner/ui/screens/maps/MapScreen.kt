package com.serqfix.partner.ui.screens.maps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    initialLocation: LatLng? = null,
    onLocationSelected: (LatLng) -> Unit = {},
    showCurrentLocation: Boolean = true
) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var currentLocation by remember { mutableStateOf<LatLng?>(initialLocation) }
    var mapProperties by remember {
        mutableStateOf(
            MapProperties(
                mapType = MapType.NORMAL,
                isMyLocationEnabled = hasLocationPermission && showCurrentLocation
            )
        )
    }
    var cameraPositionState by remember {
        mutableStateOf(
            CameraPositionState(
                position = CameraPosition.fromLatLngZoom(
                    initialLocation ?: LatLng(28.6139, 77.2090), // Default to Delhi
                    15f
                )
            )
        )
    }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && showCurrentLocation) {
            // Request location updates
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                val locationResult = fusedLocationClient.lastLocation
                locationResult.addOnSuccessListener { location: Location? ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        currentLocation = latLng
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                            )
                        }
                    }
                }
            } catch (e: SecurityException) {
                // Permission not granted
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            properties = mapProperties,
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                currentLocation = latLng
                onLocationSelected(latLng)
            }
        )
        
        // Current location button
        if (hasLocationPermission && showCurrentLocation) {
            FloatingActionButton(
                onClick = {
                    currentLocation?.let {
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(it, 15f)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Current Location"
                )
            }
        }
    }
}

@Composable
fun MapMarkerScreen(
    location: LatLng,
    title: String = "",
    snippet: String = ""
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 15f)
    }
    
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = MarkerState(position = location),
            title = title,
            snippet = snippet
        )
    }
}
