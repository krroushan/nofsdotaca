package com.serqfix.partner.ui.components.service

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class StatusInfo(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val bgColor: Color,
    val description: String
)

@Composable
fun StatusBanner(
    bookingData: com.serqfix.partner.data.api.BookingData?,
    modifier: Modifier = Modifier
) {
    if (bookingData == null) return
    
    val statusInfo = getStatusInfoFromBooking(
        canceledByCustomer = bookingData.canceledByCustomer,
        completed = bookingData.completed,
        expired = bookingData.expired,
        otpVerified = bookingData.otpVerified,
        reachedAtLocation = bookingData.reachedAtLocation,
        outForService = bookingData.outForService,
        acceptedByServiceProvider = bookingData.acceptedByServiceProvider
    )
    
    // Animate status changes
    val animatedBgColor by animateColorAsState(
        targetValue = statusInfo.bgColor,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "bgColor"
    )
    
    val animatedTextColor by animateColorAsState(
        targetValue = statusInfo.color,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "textColor"
    )
    
    AnimatedContent(
        targetState = statusInfo.label,
        transitionSpec = {
            slideInVertically { height -> height } + fadeIn() togetherWith
            slideOutVertically { height -> -height } + fadeOut()
        },
        label = "statusLabel"
    ) { label ->
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(animatedBgColor, RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = statusInfo.icon,
                contentDescription = null,
                tint = animatedTextColor,
                modifier = Modifier.size(28.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = animatedTextColor,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    text = statusInfo.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF424242)
                )
            }
        }
    }
}


// Helper function to get status info from BookingData
// This will be properly implemented once we have the full BookingData model
fun getStatusInfoFromBooking(
    canceledByCustomer: Boolean? = null,
    completed: Boolean? = null,
    expired: Boolean? = null,
    otpVerified: Boolean? = null,
    reachedAtLocation: Boolean? = null,
    outForService: Boolean? = null,
    acceptedByServiceProvider: Boolean? = null
): StatusInfo {
    return when {
        canceledByCustomer == true -> StatusInfo(
            label = "CANCELLED",
            icon = Icons.Default.Close,
            color = Color(0xFFD32F2F),
            bgColor = Color(0xFFFFEBEE),
            description = "This service was cancelled by the customer"
        )
        completed == true -> StatusInfo(
            label = "COMPLETED",
            icon = Icons.Default.CheckCircle,
            color = Color(0xFF2E7D32),
            bgColor = Color(0xFFE8F5E9),
            description = "Service has been completed successfully"
        )
        expired == true -> StatusInfo(
            label = "EXPIRED",
            icon = Icons.Default.DateRange,
            color = Color(0xFFF57C00),
            bgColor = Color(0xFFFFF3E0),
            description = "Service request has expired"
        )
        otpVerified == true -> StatusInfo(
            label = "SERVICE IN PROGRESS",
            icon = Icons.Default.Build,
            color = Color(0xFF0D47A1),
            bgColor = Color(0xFFE3F2FD),
            description = "You have reached the location and are providing service"
        )
        reachedAtLocation == true -> StatusInfo(
            label = "REACHED AT LOCATION",
            icon = Icons.Default.LocationOn,
            color = Color(0xFF0D47A1),
            bgColor = Color(0xFFE3F2FD),
            description = "You have reached the customer location, please verify OTP"
        )
        outForService == true -> StatusInfo(
            label = "OUT FOR SERVICE",
            icon = Icons.Default.ArrowForward,
            color = Color(0xFF7B1FA2),
            bgColor = Color(0xFFF3E5F5),
            description = "You are on your way to the customer location"
        )
        acceptedByServiceProvider == true -> StatusInfo(
            label = "ACCEPTED",
            icon = Icons.Default.ThumbUp,
            color = Color(0xFF1976D2),
            bgColor = Color(0xFFE3F2FD),
            description = "You have accepted this service. Please reach the location"
        )
        else -> StatusInfo(
            label = "NEW REQUEST",
            icon = Icons.Default.Notifications,
            color = Color(0xFFFFA000),
            bgColor = Color(0xFFFFF8E1),
            description = "New service request pending your response"
        )
    }
}
