package com.serqfix.partner.ui.components.service

import android.widget.Toast
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
import com.serqfix.partner.data.repository.BookingRepository
import com.serqfix.partner.utils.getCurrentLocation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CompletedOtpCard(
    bookingId: String,
    bookingRepository: BookingRepository,
    isCompletedOtpVerified: Boolean,
    onOtpVerified: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var completedOtpData by remember { mutableStateOf(OtpData()) }
    var isSendingOtp by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var timer by remember { mutableStateOf(60) }
    var isTimerActive by remember { mutableStateOf(false) }
    var hasOtpBeenSent by remember { mutableStateOf(false) }
    
    // Define verifyCompletionOtp function
    suspend fun verifyCompletionOtp(enteredOtp: String) {
        try {
            isVerifying = true
            
            val location = try {
                getCurrentLocation(context)
            } catch (e: Exception) {
                android.util.Log.w("CompletedOtpCard", "Failed to get location: ${e.message}")
                null
            }
            
            val result = bookingRepository.verifyCompletionOtp(
                bookingId = bookingId,
                otp = enteredOtp,
                lat = location?.latitude,
                lng = location?.longitude
            )
            
            result.onSuccess { response ->
                if (response.success) {
                    onOtpVerified()
                } else {
                    android.widget.Toast.makeText(
                        context,
                        response.message ?: "OTP verification failed",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }.onFailure { error ->
                android.widget.Toast.makeText(
                    context,
                    "Failed to verify OTP: ${error.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                context,
                "Error: ${e.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } finally {
            isVerifying = false
        }
    }
    
    // Timer countdown
    LaunchedEffect(isTimerActive) {
        while (isTimerActive && timer > 0) {
            delay(1000)
            timer--
        }
        if (timer == 0) {
            isTimerActive = false
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Service Completion Verification",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            
            if (!isCompletedOtpVerified) {
                Text(
                    text = "After completing the service, request customer to provide OTP for verification",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
                
                // Info message
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Send OTP to customer and ask them to enter the received OTP below",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF424242)
                    )
                }
                
                // OTP Input
                OtpInput(
                    otpData = completedOtpData,
                    onOtpChange = { index, value ->
                        completedOtpData = completedOtpData.copy(
                            otp = completedOtpData.otp.toMutableList().apply {
                                if (index < size) {
                                    this[index] = value
                                }
                            }
                        )
                    },
                    isOtpVerified = isCompletedOtpVerified,
                    disabled = false
                )
                
                // Timer display
                if (isTimerActive && timer > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Resend OTP in ${timer}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
                
                // Verify OTP Button
                Button(
                    onClick = {
                        val enteredOtp = completedOtpData.otp.joinToString("")
                        if (enteredOtp.length == 4) {
                            scope.launch {
                                verifyCompletionOtp(enteredOtp)
                            }
                        }
                    },
                    enabled = completedOtpData.otp.joinToString("").length == 4 && !isVerifying,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verify OTP")
                }
            } else {
                // Verified state
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Service completion OTP verified successfully!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
