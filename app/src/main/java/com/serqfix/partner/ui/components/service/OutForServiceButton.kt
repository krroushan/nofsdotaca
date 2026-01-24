package com.serqfix.partner.ui.components.service

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.serqfix.partner.data.repository.BookingRepository
import com.serqfix.partner.utils.getCurrentLocation
import kotlinx.coroutines.launch

@Composable
fun OutForServiceButton(
    bookingId: String,
    providerId: String,
    displayBookingId: String? = null,
    bookingRepository: BookingRepository,
    onStatusUpdated: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var isConfirmed by remember { mutableStateOf(false) }
    
    SlideToConfirmButton(
        text = "Slide to mark 'Out For Service'",
        confirmText = "On Your Way! 👍",
        slideIcon = Icons.Default.ArrowForward,
        confirmIcon = Icons.Default.CheckCircle,
        backgroundColor = Color(0xFF3954A4),
        isLoading = isLoading,
        isConfirmed = isConfirmed,
        onConfirm = {
            scope.launch {
                try {
                    isLoading = true
                    
                    // Get current location
                    val location = try {
                        getCurrentLocation(context)
                    } catch (e: Exception) {
                        android.util.Log.w("OutForServiceButton", "Failed to get location: ${e.message}")
                        null
                    }
                    
                    // Update booking status
                    val result = bookingRepository.updateBookingStatus(
                        bookingId = bookingId,
                        providerId = providerId,
                        statusType = "outForService",
                        value = true,
                        lat = location?.latitude,
                        lng = location?.longitude
                    )
                    
                    result.onSuccess { response ->
                        if (response.success) {
                            isConfirmed = true
                            Toast.makeText(
                                context,
                                response.message ?: "You're on your way to the service location!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onStatusUpdated(true)
                        } else {
                            Toast.makeText(
                                context,
                                response.message ?: "Failed to update status",
                                Toast.LENGTH_SHORT
                            ).show()
                            isLoading = false
                        }
                    }.onFailure { error ->
                        Toast.makeText(
                            context,
                            error.message ?: "Failed to update status. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                        isLoading = false
                    }
                } catch (e: Exception) {
                    android.util.Log.e("OutForServiceButton", "Error: ${e.message}", e)
                    Toast.makeText(
                        context,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    isLoading = false
                }
            }
        },
        modifier = modifier
    )
}
