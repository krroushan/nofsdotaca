package com.serqfix.partner.ui.components.service

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Context
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.serqfix.partner.utils.calculateDistance
import com.serqfix.partner.utils.estimateTravelTime

@Composable
fun MapLocationCard2(
    destinationLat: Double?,
    destinationLng: Double?,
    address: String? = null,
    userLat: Double? = null,
    userLng: Double? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Initialize Google Maps
    LaunchedEffect(Unit) {
        com.google.android.gms.maps.MapsInitializer.initialize(context)
    }
    
    if (destinationLat == null || destinationLng == null) {
        // Show placeholder if location not available
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Location not available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
        }
        return
    }
    
    val destination = LatLng(destinationLat, destinationLng)
    val userLocation = if (userLat != null && userLng != null) {
        LatLng(userLat, userLng)
    } else {
        null
    }
    
    // Calculate camera position
    val cameraPosition = remember(destination, userLocation) {
        if (userLocation != null) {
            // Center between user and destination
            val centerLat = (destinationLat + userLat!!) / 2
            val centerLng = (destinationLng + userLng!!) / 2
            CameraPosition.fromLatLngZoom(
                LatLng(centerLat, centerLng),
                13f
            )
        } else {
            CameraPosition.fromLatLngZoom(
                destination,
                15f
            )
        }
    }
    
    // Calculate distance and ETA
    val distance = remember(destinationLat, destinationLng, userLat, userLng) {
        if (userLat != null && userLng != null) {
            calculateDistance(userLat, userLng, destinationLat, destinationLng)
        } else {
            null
        }
    }
    
    val eta = remember(distance) {
        distance?.let { estimateTravelTime(it) }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = rememberCameraPositionState {
                        position = cameraPosition
                    },
                    properties = MapProperties(
                        mapType = MapType.NORMAL,
                        isMyLocationEnabled = userLocation != null
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        myLocationButtonEnabled = false,
                        compassEnabled = true
                    )
                ) {
                    // Destination marker
                    Marker(
                        state = MarkerState(position = destination),
                        title = "Service Location",
                        snippet = address
                    )
                    
                    // User location marker
                    if (userLocation != null) {
                        Marker(
                            state = MarkerState(position = userLocation),
                            title = "Your Location",
                            icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                                com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_BLUE
                            )
                        )
                    }
                }
            }
            
            // Info section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Address
                if (!address.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = address,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Distance and ETA
                if (distance != null && eta != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${String.format("%.1f", distance)} km",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "~$eta min",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2196F3),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Directions button
                Button(
                    onClick = {
                        // Open Google Maps with directions
                        val uri = Uri.parse("google.navigation:q=$destinationLat,$destinationLng")
                        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            // Fallback to web maps
                            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destinationLat,$destinationLng")
                            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                            context.startActivity(webIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Get Directions")
                }
            }
        }
    }
}
