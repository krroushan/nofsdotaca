package com.prashantpizza.nofsdotaca.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prashantpizza.nofsdotaca.repository.Order
import com.prashantpizza.nofsdotaca.repository.OrdersRepository
import kotlinx.coroutines.launch

@Composable
fun OrdersScreen(
    onOrderClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val ordersRepository = remember { OrdersRepository.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    
    // Fetch orders on first load
    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            errorMessage = null
            val result = ordersRepository.getOrders(limit = 50, page = 1)
            result.onSuccess { response ->
                orders = response.data ?: emptyList()
                isLoading = false
            }.onFailure { exception ->
                errorMessage = exception.message ?: "Failed to load orders"
                isLoading = false
            }
        }
    }
    
    // Pull to refresh handler
    fun refreshOrders() {
        scope.launch {
            refreshing = true
            errorMessage = null
            val result = ordersRepository.getOrders(limit = 50, page = 1)
            result.onSuccess { response ->
                orders = response.data ?: emptyList()
                refreshing = false
            }.onFailure { exception ->
                errorMessage = exception.message ?: "Failed to refresh orders"
                refreshing = false
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
        // Error message
        if (errorMessage != null && !isLoading) {
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
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { refreshOrders() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
        
        // Loading state
        if (isLoading) {
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
                            text = "Loading orders...",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Empty state
        if (!isLoading && errorMessage == null && orders.isEmpty()) {
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
                        Text(
                            text = "No orders yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Orders will appear here when customers place them",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { refreshOrders() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Refresh")
                        }
                    }
                }
            }
        }
        
        // Orders list
        if (!isLoading && errorMessage == null) {
            items(orders) { order ->
                OrderCard(
                    order = order,
                    onClick = { onOrderClick(order._id) }
                )
            }
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White // White card background
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderNumber,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // Status badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (order.status?.lowercase()) {
                        "pending" -> MaterialTheme.colorScheme.tertiaryContainer
                        "confirmed", "preparing" -> MaterialTheme.colorScheme.primaryContainer
                        "ready", "out_for_delivery" -> MaterialTheme.colorScheme.secondaryContainer
                        "delivered", "completed" -> MaterialTheme.colorScheme.primaryContainer
                        "cancelled", "rejected" -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = order.status?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: "Unknown",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (order.status?.lowercase()) {
                            "pending" -> MaterialTheme.colorScheme.onTertiaryContainer
                            "confirmed", "preparing" -> MaterialTheme.colorScheme.onPrimaryContainer
                            "ready", "out_for_delivery" -> MaterialTheme.colorScheme.onSecondaryContainer
                            "delivered", "completed" -> MaterialTheme.colorScheme.onPrimaryContainer
                            "cancelled", "rejected" -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Customer info
            if (order.customer?.name != null) {
                Text(
                    text = "Customer: ${order.customer.name}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Items summary
            if (!order.items.isNullOrEmpty()) {
                val itemsText = order.items.joinToString(", ") { 
                    "${it.quantity ?: 1}x ${it.productName ?: "Item"}"
                }
                Text(
                    text = itemsText,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Total amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: ₹${String.format("%.2f", order.total ?: 0.0)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Payment status
                if (order.payment?.status != null) {
                    Text(
                        text = "Payment: ${order.payment.status.replaceFirstChar { it.uppercase() }}",
                        fontSize = 12.sp,
                        color = when (order.payment.status.lowercase()) {
                            "paid" -> MaterialTheme.colorScheme.primary
                            "pending" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            // Order type and source
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (order.orderType != null) {
                    Text(
                        text = order.orderType.replaceFirstChar { it.uppercase() },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (order.source != null) {
                    Text(
                        text = "• ${order.source.replaceFirstChar { it.uppercase() }}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
