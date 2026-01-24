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
import kotlinx.coroutines.launch

@Composable
fun ReachedOtpCard(
    bookingId: String,
    bookingRepository: BookingRepository,
    isOutForService: Boolean,
    isReachedAtLocation: Boolean,
    isOtpVerified: Boolean,
    onOtpVerified: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var otpData by remember { mutableStateOf(OtpData()) }
    var otpError by remember { mutableStateOf<String?>(null) }
    var isVerifying by remember { mutableStateOf(false) }
    
    val canEnterOtp = isOutForService && isReachedAtLocation
    
    // Define verifyOtp function
    suspend fun verifyOtp(enteredOtp: String) {
        try {
            val location = try {
                getCurrentLocation(context)
            } catch (e: Exception) {
                android.util.Log.w("ReachedOtpCard", "Failed to get location: ${e.message}")
                null
            }
            
            val result = bookingRepository.verifyReachedOtp(
                bookingId = bookingId,
                otp = enteredOtp,
                lat = location?.latitude,
                lng = location?.longitude
            )
            
            result.onSuccess { response ->
                if (response.success) {
                    onOtpVerified()
                    Toast.makeText(
                        context,
                        "OTP verified successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    otpError = response.message ?: "Invalid OTP. Please try again."
                    isVerifying = false
                }
            }.onFailure { error ->
                otpError = error.message ?: "Failed to verify OTP. Please try again."
                isVerifying = false
            }
        } catch (e: Exception) {
            android.util.Log.e("ReachedOtpCard", "Error verifying OTP: ${e.message}", e)
            otpError = "Error: ${e.message}"
            isVerifying = false
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
                text = "Enter Reached OTP",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            
            // Warning message if conditions not met
            if (!canEnterOtp && !isOtpVerified) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = when {
                            !isOutForService -> "You must first mark yourself as 'Out For Service' before you can enter the OTP."
                            else -> "You must mark yourself as 'Reached At Location' before you can enter the OTP."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF424242)
                    )
                }
            }
            
            // OTP Input
            OtpInput(
                otpData = otpData,
                onOtpChange = { index, value ->
                    otpData = otpData.copy(
                        otp = otpData.otp.toMutableList().apply {
                            if (index < size) {
                                this[index] = value
                            }
                        }
                    )
                    otpError = null
                    
                    // Auto-verify when all 4 digits are entered
                    if (otpData.otp.all { it.isNotEmpty() } && canEnterOtp && !isOtpVerified) {
                        val enteredOtp = otpData.otp.joinToString("")
                        scope.launch {
                            isVerifying = true
                            verifyOtp(enteredOtp)
                        }
                    }
                },
                isOtpVerified = isOtpVerified,
                disabled = !canEnterOtp && !isOtpVerified
            )
            
            // Error message
            if (otpError != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = otpError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF44336)
                    )
                }
            }
            
            // Auto-verification indicator
            if (otpData.otp.all { it.isNotEmpty() } && !isOtpVerified && canEnterOtp && isVerifying) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF3954A4)
                    )
                    Text(
                        text = "Verifying OTP...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF3954A4)
                    )
                }
            }
            
            // Verified success message
            if (isOtpVerified) {
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
                        text = "OTP Verified Successfully",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
