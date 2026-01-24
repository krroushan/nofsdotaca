package com.serqfix.partner.ui.components.service

import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun SlideToConfirmButton(
    text: String = "Slide to confirm",
    confirmText: String = "Confirmed!",
    confirmIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.CheckCircle,
    slideIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.ArrowForward,
    backgroundColor: Color = Color(0xFF3954A4),
    textColor: Color = Color.White,
    isLoading: Boolean = false,
    isConfirmed: Boolean = false,
    disabled: Boolean = false,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderWidth by remember { mutableStateOf(0f) }
    var thumbWidth by remember { mutableStateOf(50f) }
    var dragOffset by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()
    
    val maxSlide = remember(sliderWidth, thumbWidth) {
        (sliderWidth - thumbWidth - 10f).coerceAtLeast(1f)
    }
    
    val progress = remember(dragOffset, maxSlide) {
        if (maxSlide > 0) (dragOffset / maxSlide).coerceIn(0f, 1f) else 0f
    }
    
    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Bounce animation
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    
    // Arrow opacity animation
    val arrowOpacity by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow"
    )
    
    val progressColor = animateColorAsState(
        targetValue = when {
            isConfirmed -> Color(0xFF2E7D32)
            progress >= 0.6f -> Color(0xFF2E7D32)
            else -> backgroundColor
        },
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "progressColor"
    )
    
    // Animate thumb position
    val animatedThumbOffset by animateFloatAsState(
        targetValue = dragOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "thumbOffset"
    )
    
    val shouldAnimate = !isConfirmed && !isLoading && !disabled
    
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(if (disabled) Color(0xFFCCCCCC) else backgroundColor)
            .pointerInput(Unit) {
                if (!disabled && !isLoading && !isConfirmed) {
                    detectDragGestures(
                        onDragEnd = {
                            val threshold = maxSlide * 0.6f
                            if (dragOffset >= threshold) {
                                // Complete the slide
                                scope.launch {
                                    dragOffset = maxSlide
                                    onConfirm()
                                }
                            } else {
                                // Return to start
                                scope.launch {
                                    dragOffset = 0f
                                }
                            }
                        }
                    ) { change, dragAmount ->
                        val newOffset = (dragOffset + dragAmount.x).coerceIn(0f, maxSlide)
                        dragOffset = newOffset
                    }
                }
            }
            .onSizeChanged { size ->
                sliderWidth = size.width.toFloat()
            }
    ) {
        // Progress bar background
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(progressColor.value)
        )
        
        // Text container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isConfirmed) confirmText else text,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .scale(if (shouldAnimate) pulseScale else 1f)
            )
        }
        
        // Thumb button with animation
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (animatedThumbOffset + 3f).dp)
                .offset(x = if (shouldAnimate) bounceOffset.dp else 0.dp)
                .size(50.dp)
                .clip(CircleShape)
                .background(if (isConfirmed) Color(0xFF2E7D32) else Color.White)
                .onSizeChanged { size ->
                    thumbWidth = size.width.toFloat()
                },
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = Triple(isLoading, isConfirmed, shouldAnimate),
                transitionSpec = {
                    scaleIn(animationSpec = spring()) + fadeIn() togetherWith
                    scaleOut(animationSpec = spring()) + fadeOut()
                },
                label = "thumbIcon"
            ) { (loading, confirmed, animate) ->
                when {
                    loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = backgroundColor,
                            strokeWidth = 2.dp
                        )
                    }
                    confirmed -> {
                        Icon(
                            imageVector = confirmIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = slideIcon,
                            contentDescription = null,
                            tint = backgroundColor,
                            modifier = Modifier
                                .size(24.dp)
                                .alpha(if (animate) arrowOpacity else 0.5f)
                        )
                    }
                }
            }
        }
        
        // Direction indicator (arrows)
        if (shouldAnimate) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 60.dp)
                    .alpha(arrowOpacity * 0.3f),
                horizontalArrangement = Arrangement.spacedBy((-8).dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    
    // Reset drag offset when confirmed state changes
    LaunchedEffect(isConfirmed) {
        if (!isConfirmed) {
            dragOffset = 0f
        }
    }
}
