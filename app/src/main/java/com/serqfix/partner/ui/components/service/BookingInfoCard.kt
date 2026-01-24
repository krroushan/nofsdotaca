package com.serqfix.partner.ui.components.service

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.serqfix.partner.data.api.BookingData

@Composable
fun BookingInfoCard(
    bookingData: BookingData?,
    modifier: Modifier = Modifier
) {
    if (bookingData == null) return
    
    Column(modifier = modifier) {
        // Header with Booking ID
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Booking ID: #${bookingData.bookingId ?: "N/A"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF424242)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Info Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section Header with Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Booking Info",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242)
                    )
                    
                    // Status Badge
                    StatusBadge(
                        status = when {
                            bookingData.canceledByCustomer == true -> "cancelled"
                            bookingData.completed == true -> "completed"
                            bookingData.expired == true -> "expired"
                            bookingData.otpVerified == true -> "otpVerified"
                            bookingData.acceptedByServiceProvider == true -> "accepted"
                            else -> "default"
                        }
                    )
                }
                
                // Service Items
                if (!bookingData.cartItems.isNullOrEmpty()) {
                    bookingData.cartItems.forEach { item ->
                        ServiceItemCard(
                            item = item,
                            bookingDate = bookingData.date ?: bookingData.serviceDate,
                            bookingTime = bookingData.time ?: bookingData.serviceTime
                        )
                    }
                } else {
                    // No service items
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF7F3), RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF7A00),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "No service items found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFF7A00)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceItemCard(
    item: com.serqfix.partner.data.api.CartItem,
    bookingDate: String?,
    bookingTime: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Service details row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Service image
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp))
            ) {
                if (!item.icon?.url.isNullOrEmpty()) {
                    AsyncImage(
                        model = item.icon?.url ?: "",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E)
                        )
                    }
                }
            }
            
            // Service details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title ?: "Unnamed Service",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF424242)
                )
                Text(
                    text = "Qty: ${item.quantity ?: 1}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
            }
        }
        
        // Date and Time badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date badge
            if (bookingDate != null) {
                Row(
                    modifier = Modifier
                        .background(Color(0xFF4CAF50), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = bookingDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Time badge
            if (bookingTime != null) {
                Row(
                    modifier = Modifier
                        .background(Color(0xFF2196F3), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                            imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = formatTimeToAMPM(bookingTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun formatTimeToAMPM(time: String): String {
    // Simple time formatting - can be enhanced
    return try {
        val parts = time.split(":")
        if (parts.size >= 2) {
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            val amPm = if (hour < 12) "AM" else "PM"
            val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            String.format("%d:%02d %s", displayHour, minute, amPm)
        } else {
            time
        }
    } catch (e: Exception) {
        time
    }
}
