package com.serqfix.partner.ui.components.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.serqfix.partner.data.api.UserData
import com.serqfix.partner.ui.navigation.NavRoute

@Composable
fun HomepageHeader(
    userData: UserData?,
    available: Boolean,
    onAvailabilityChange: () -> Unit,
    navController: NavController,
    unreadCount: Int = 0,
    isLoading: Boolean = false
) {
    val statusColor by animateColorAsState(
        targetValue = if (available) Color(0xFF4CAF50) else Color(0xFFFF5252),
        animationSpec = tween(durationMillis = 300),
        label = "status_color"
    )
    
    var scale by remember { mutableStateOf(1f) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                )
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
            // Avatar and User Info
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { navController.navigate(NavRoute.Profile.route) },
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .scale(scale)
                ) {
                    Box(
                        modifier = Modifier.size(48.dp)
                    ) {
                        // Avatar Image
                        val imageUrl = userData?.image?.url
                        if (imageUrl != null) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(imageUrl)
                                        .crossfade(true)
                                        .build()
                                ),
                                contentDescription = "User avatar",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, Color(0x4D4CAF50), CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Default avatar placeholder
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(color = Color(0xFFE0E0E0), shape = CircleShape)
                                    .border(1.5.dp, Color(0x4D4CAF50), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userData?.name?.take(1)?.uppercase() ?: "?",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color(0xFF757575)
                                )
                            }
                        }
                        
                        // Status Indicator
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(color = statusColor, shape = CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Hi ${userData?.name ?: "there"}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 0.2.sp
                        ),
                        color = Color(0xFF1A1A1A),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (available) "Ready for New Services" else "Currently Unavailable",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = statusColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Notification Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clickable {
                        navController.navigate(NavRoute.Notifications.route)
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color(0xFF1A1A1A),
                    modifier = Modifier.size(24.dp)
                )
                
                if (unreadCount > 0) {
                    Badge(
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Switch Button
            SwitchButton(
                available = available,
                onToggle = onAvailabilityChange,
                isLoading = isLoading
            )
    }
}
