package com.serqfix.partner.ui.components.service

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ServiceDetailsSkeletonLoader(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Banner Skeleton
        SkeletonCard(height = 80.dp, alpha = alpha)
        
        // Map Skeleton
        SkeletonCard(height = 350.dp, alpha = alpha)
        
        // Booking Info Skeleton
        SkeletonCard(height = 200.dp, alpha = alpha)
        
        // Action Buttons Skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SkeletonButton(modifier = Modifier.weight(1f), alpha = alpha)
            SkeletonButton(modifier = Modifier.weight(1f), alpha = alpha)
        }
    }
}

@Composable
private fun SkeletonCard(
    height: androidx.compose.ui.unit.Dp,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha)
                .background(Color(0xFFE0E0E0))
        )
    }
}

@Composable
private fun SkeletonButton(
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .alpha(alpha)
            .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
    )
}
