package com.serqfix.partner.ui.components.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SwitchButton(
    available: Boolean,
    onToggle: () -> Unit,
    isLoading: Boolean = false
) {
    val animatedPosition by animateFloatAsState(
        targetValue = if (available) 28f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "switch_position"
    )
    
    val backgroundColor = if (available) Color(0xFFA5D6A7) else Color(0xFFE0E0E0)
    val circleColor = if (available) Color(0xFF388E3C) else Color(0xFF000000)
    
    Box(
        modifier = Modifier
            .width(83.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (isLoading) backgroundColor.copy(alpha = 0.7f) else backgroundColor)
            .clickable(enabled = !isLoading) { onToggle() },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = animatedPosition.dp)
                .width(50.dp)
                .height(26.dp)
                .padding(horizontal = 2.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(circleColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (available) "ONLINE" else "OFFLINE",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
