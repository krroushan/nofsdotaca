package com.serqfix.partner.ui.components.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.serqfix.partner.data.api.BookingData
import com.serqfix.partner.ui.navigation.NavRoute
import com.serqfix.partner.utils.DateTimeUtils

@Composable
fun ServiceCard(
    booking: BookingData,
    navController: NavController
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(),
        label = "card_scale"
    )
    
    // Get cart images (max 3)
    val cartImages = booking.cartItems?.take(3)?.mapNotNull { item ->
        item.icon?.url
    } ?: emptyList()
    
    // Calculate total price
    val total = booking.cartItems?.sumOf { item ->
        (item.price ?: 0.0) * (item.quantity ?: 1)
    } ?: booking.amount ?: 0.0
    
    // Get status colors
    val statusColors = getStatusColors(booking.status ?: "")
    val statusText = getStatusText(booking.status ?: "")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .scale(scale)
            .clickable {
                navController.navigate("${NavRoute.ServiceDetails.route}?bookingId=${booking._id ?: booking.bookingId}")
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColors.bgColor)
    ) {
        Column {
            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFAFAFA))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Service Images
                    if (cartImages.isNotEmpty()) {
                        if (cartImages.size == 1) {
                            // Single image
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(cartImages[0])
                                        .crossfade(true)
                                        .build()
                                ),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Stacked images
                            Box(
                                modifier = Modifier.size(72.dp, 72.dp)
                            ) {
                                cartImages.forEachIndexed { index, imageUrl ->
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            ImageRequest.Builder(LocalContext.current)
                                                .data(imageUrl)
                                                .crossfade(true)
                                                .build()
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(58.dp)
                                            .offset(x = (index * 20).dp)
                                            .clip(CircleShape)
                                            .border(2.dp, Color.White, CircleShape)
                                            .zIndex((cartImages.size - index).toFloat()),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Booking #${booking.bookingId ?: ""}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = Color(0xFF212121)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // Status Badge
                        Row(
                            modifier = Modifier
                                .background(statusColors.bgColor, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(statusColors.dotColor)
                            )
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = statusColors.textColor
                            )
                        }
                    }
                }
                
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Address Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Address
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = booking.address ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp
                        ),
                        color = Color(0xFF424242),
                        maxLines = 2
                    )
                }
                
                // Date and Time
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = booking.date ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF424242)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = DateTimeUtils.formatTimeToAMPM(booking.time),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF424242)
                        )
                    }
                }
            }
            
            // Footer - Total Amount
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
                    .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(0.dp)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Amount",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFF757575)
                )
                Text(
                    text = "₹${total.toInt()}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}

private fun getStatusColors(status: String): StatusColors {
    return when {
        status == "Active" -> StatusColors(
            bgColor = Color(0xFFE8F5E9),
            textColor = Color(0xFF2E7D32),
            dotColor = Color(0xFF4CAF50)
        )
        status.contains("assigned", ignoreCase = true) -> StatusColors(
            bgColor = Color(0xFFE3F2FD),
            textColor = Color(0xFF1565C0),
            dotColor = Color(0xFF2196F3)
        )
        else -> StatusColors(
            bgColor = Color(0xFFFFF3E0),
            textColor = Color(0xFFE65100),
            dotColor = Color(0xFFFF9800)
        )
    }
}

private fun getStatusText(status: String): String {
    return when {
        status == "Active" -> "Active"
        status.contains("assigned", ignoreCase = true) -> "Assigned to you"
        status.contains("has been reached!", ignoreCase = true) -> "Ongoing"
        else -> status
    }
}

private data class StatusColors(
    val bgColor: Color,
    val textColor: Color,
    val dotColor: Color
)
