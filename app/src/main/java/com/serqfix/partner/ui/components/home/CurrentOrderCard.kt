package com.serqfix.partner.ui.components.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.serqfix.partner.data.api.BookingData
import com.serqfix.partner.ui.navigation.NavRoute
import com.serqfix.partner.utils.DateTimeUtils

@Composable
fun CurrentOrderCard(
    currentBooking: BookingData,
    serviceProviderId: String,
    isAvailable: Boolean,
    onAccept: (String) -> Unit,
    navController: NavController
) {
    var isCardVisible by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Get first cart item
    val firstCartItem = currentBooking.cartItems?.firstOrNull()
    val serviceName = firstCartItem?.title ?: "Unnamed Service"
    val bookingId = currentBooking._id ?: currentBooking.bookingId ?: ""
    
    // Simplified structure without nested AnimatedVisibility
    if (!isCardVisible) {
        // Show minimized button
        Button(
            onClick = { isCardVisible = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2E7D32)
            ),
            shape = RoundedCornerShape(
                topStart = 15.dp,
                topEnd = 15.dp,
                bottomStart = 0.dp,
                bottomEnd = 0.dp
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Order Available",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    } else {
        // Show full card
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .zIndex(1000f),
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2E7D32)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isAvailable) {
                        navController.navigate("${NavRoute.ServiceDetails.route}?bookingId=$bookingId")
                    }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Column - Booking Info
                    Column(
                        modifier = Modifier.weight(3f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Booking ID and Service Name
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${currentBooking.bookingId ?: ""}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE8F5E9)
                                )
                            )
                            Text(
                                text = serviceName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE8F5E9)
                                )
                            )
                        }
                        
                        // Date and Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = Color(0xFFE8F5E9),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = currentBooking.date ?: "",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFE8F5E9)
                                    )
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = Color(0xFFE8F5E9),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = DateTimeUtils.formatTimeToAMPM(currentBooking.time),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFE8F5E9)
                                    )
                                )
                            }
                        }
                        
                        // Address
                        Row(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color(0xFFE8F5E9),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = currentBooking.address ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE8F5E9)
                                ),
                                maxLines = 2
                            )
                        }
                    }
                    
                    // Right Column - Actions
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Close Button
                        IconButton(
                            onClick = { isCardVisible = false },
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color(0xFFE8F5E9), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Accept Button
                        Button(
                            onClick = {
                                if (isAvailable && !isLoading) {
                                    isLoading = true
                                    onAccept(bookingId)
                                }
                            },
                            enabled = isAvailable && !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE8F5E9),
                                disabledContainerColor = Color(0xFFE8F5E9).copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color(0xFF2E7D32),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Accept",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
