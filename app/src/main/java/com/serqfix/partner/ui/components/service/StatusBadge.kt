package com.serqfix.partner.ui.components.service

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, statusText) = when (status) {
        "completed" -> Triple(
            Color(0xFFE8F6E8),
            Color(0xFF2E7D32),
            "Completed"
        )
        "cancelled" -> Triple(
            Color(0xFFFFCDD2),
            Color(0xFFF44336),
            "Cancelled"
        )
        "expired" -> Triple(
            Color(0xFFFFCDD2),
            Color(0xFFF44336),
            "Expired"
        )
        "otpVerified" -> Triple(
            Color(0xFF90CAF9),
            Color(0xFF1565C0),
            "Service in Progress"
        )
        "reachedAtLocation" -> Triple(
            Color(0xFFE3F2FD),
            Color(0xFF0D47A1),
            "Reached at Location"
        )
        "outForService" -> Triple(
            Color(0xFFF3E5F5),
            Color(0xFF7B1FA2),
            "Out For Service"
        )
        "accepted" -> Triple(
            Color(0xFFFFE082),
            Color(0xFFF57F17),
            "Accepted"
        )
        else -> Triple(
            Color(0xFFFCFFBC),
            Color(0xFFB1B000),
            "Ready to Accept"
        )
    }
    
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(9999.dp))
            .padding(horizontal = 15.dp, vertical = 5.dp)
    ) {
        Text(
            text = statusText,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
