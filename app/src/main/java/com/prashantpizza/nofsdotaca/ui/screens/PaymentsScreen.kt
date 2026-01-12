package com.prashantpizza.nofsdotaca.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prashantpizza.nofsdotaca.repository.PaymentsRepository
import com.prashantpizza.nofsdotaca.utils.DateUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PaymentsScreen() {
    val context = LocalContext.current
    val paymentsRepository = remember { PaymentsRepository.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    var payments by remember { mutableStateOf<List<com.prashantpizza.nofsdotaca.repository.PaymentRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Fetch payments on first load
    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            errorMessage = null
            val result = paymentsRepository.getPayments(limit = 100)
            result.onSuccess { response ->
                payments = response.data ?: emptyList()
                isLoading = false
            }.onFailure { exception ->
                errorMessage = exception.message ?: "Failed to load payments"
                isLoading = false
            }
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Light gray background
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            isLoading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Loading payments...",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            errorMessage != null -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Error",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            payments.isEmpty() -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No payments yet",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                items(payments) { payment ->
                    PaymentCard(payment = payment)
                }
            }
        }
    }
}

@Composable
fun PaymentCard(payment: com.prashantpizza.nofsdotaca.repository.PaymentRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White // White card background
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = payment.orderNumber,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Payment status badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (payment.payment?.status?.lowercase()) {
                        "paid" -> MaterialTheme.colorScheme.primaryContainer
                        "pending" -> MaterialTheme.colorScheme.tertiaryContainer
                        "refunded" -> MaterialTheme.colorScheme.secondaryContainer
                        "failed" -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = payment.payment?.status?.replaceFirstChar { it.uppercase() } ?: "Unknown",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            if (payment.customer != null) {
                if (payment.customer.name != null) {
                    Text(
                        text = "Customer: ${payment.customer.name}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (payment.customer.phone != null) {
                    Text(
                        text = "Phone: ${payment.customer.phone}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    if (payment.payment?.method != null) {
                        Text(
                            text = "Method: ${payment.payment.method.replaceFirstChar { it.uppercase() }}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (payment.orderType != null) {
                        Text(
                            text = "Type: ${payment.orderType.replaceFirstChar { it.uppercase() }}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    if (payment.total != null) {
                        Text(
                            text = "₹${String.format("%.2f", payment.total)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (payment.createdAt != null) {
                        Text(
                            text = DateUtils.formatDate(payment.createdAt),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (payment.payment?.transactionId != null && payment.payment.transactionId.isNotEmpty()) {
                Text(
                    text = "Transaction ID: ${payment.payment.transactionId}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

