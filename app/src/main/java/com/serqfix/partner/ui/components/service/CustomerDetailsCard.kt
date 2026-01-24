package com.serqfix.partner.ui.components.service

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.serqfix.partner.data.api.BookingData
import kotlinx.coroutines.launch

@Composable
fun CustomerDetailsCard(
    bookingData: BookingData?,
    providerPhoneNumber: String? = null,
    onCallError: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (bookingData == null) return
    
    val context = LocalContext.current
    var isCalling by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val customerName = bookingData.customerName ?: bookingData.fullname ?: "Customer"
    val customerPhone = bookingData.customerPhone ?: bookingData.phoneNumber
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Customer Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Customer info
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFFF5F5F5), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color(0xFF424242),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    // Customer name
                    Text(
                        text = customerName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF212121)
                    )
                }
                
                // Call button
                if (customerPhone != null && providerPhoneNumber != null) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                handleIVRCall(
                                    context = context,
                                    providerPhone = providerPhoneNumber,
                                    customerPhone = customerPhone,
                                    onStart = { isCalling = true },
                                    onComplete = { isCalling = false },
                                    onError = { error ->
                                        isCalling = false
                                        onCallError(error)
                                    }
                                )
                            }
                        },
                        enabled = !isCalling,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (isCalling) Color(0xFF4CAF50).copy(alpha = 0.6f)
                                else Color(0xFF4CAF50),
                                CircleShape
                            )
                    ) {
                        if (isCalling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Call Customer",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun handleIVRCall(
    context: android.content.Context,
    providerPhone: String,
    customerPhone: String,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onError: (String) -> Unit
) {
    onStart()
    
    try {
        // Format phone numbers
        val formatPhoneNumber: (String) -> String = { phone ->
            if (phone.isEmpty()) {
                ""
            } else {
                var formatted = phone.replace("\\s+".toRegex(), "").replace("[^\\d+]".toRegex(), "")
                if (!formatted.startsWith("+")) {
                    if (formatted.startsWith("0")) {
                        formatted = formatted.substring(1)
                    }
                    formatted = "+91$formatted"
                }
                formatted
            }
        }
        
        val firstPhone = formatPhoneNumber(providerPhone)
        val secondPhone = formatPhoneNumber(customerPhone)
        val did = "+911294914387" // TODO: Get from config
        
        // Call IVR API
        val url = java.net.URL("https://api.ivrsolutions.in/api/click_connect")
        val connection = url.openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer e1274b26156a7f4bab1e0f30d71bc542")
        connection.doOutput = true
        
        val requestBody = """
            {
                "first_phone": "$firstPhone",
                "second_phone": "$secondPhone",
                "did": "$did"
            }
        """.trimIndent()
        
        connection.outputStream.use { it.write(requestBody.toByteArray()) }
        
        val responseCode = connection.responseCode
        val responseBody = if (responseCode == 200) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
        }
        
        connection.disconnect()
        
        // Parse response
        val jsonResponse = org.json.JSONObject(responseBody)
        
        if (jsonResponse.optInt("status") == 200) {
            val data = jsonResponse.optJSONObject("data")
            val innerData = data?.optJSONObject("data")
            val didNo = innerData?.optString("did_no")
            
            if (!didNo.isNullOrEmpty()) {
                // Open phone dialer
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    setData(Uri.parse("tel:$didNo"))
                }
                context.startActivity(dialIntent)
                onComplete()
            } else {
                onError("Failed to get DID number from response")
            }
        } else {
            val message = jsonResponse.optString("message", "Failed to initiate call")
            onError(message)
        }
    } catch (e: Exception) {
        onError("Failed to initiate call: ${e.message}")
    } finally {
        onComplete()
    }
}
