package com.prashantpizza.nofsdotaca.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Receipt
import com.prashantpizza.nofsdotaca.repository.Order
import com.prashantpizza.nofsdotaca.repository.OrderHistoryEntry
import com.prashantpizza.nofsdotaca.repository.OrderItem
import com.prashantpizza.nofsdotaca.repository.OrdersRepository
import com.prashantpizza.nofsdotaca.utils.DateUtils
import com.prashantpizza.nofsdotaca.utils.buildOrderBillBreakdown
import com.prashantpizza.nofsdotaca.utils.formatMoney
import kotlinx.coroutines.launch
import androidx.core.view.WindowCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val ordersRepository = remember { OrdersRepository.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    var order by remember { mutableStateOf<Order?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Get color in composable context
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    
    // Set status bar color to match app bar
    LaunchedEffect(primaryContainerColor) {
        val window = (view.context as? android.app.Activity)?.window
        window?.let {
            // Convert Compose Color to Android Color
            val colorValue = primaryContainerColor.value
            val red = ((colorValue shr 16) and 0xFFu).toInt()
            val green = ((colorValue shr 8) and 0xFFu).toInt()
            val blue = (colorValue and 0xFFu).toInt()
            val alpha = ((colorValue shr 24) and 0xFFu).toInt()
            
            val statusBarColor = android.graphics.Color.argb(alpha, red, green, blue)
            
            it.statusBarColor = statusBarColor
            WindowCompat.setDecorFitsSystemWindows(it, false)
            
            val insetsController = WindowCompat.getInsetsController(it, view)
            // Use light status bar icons (dark icons) since primaryContainer is typically light
            insetsController?.isAppearanceLightStatusBars = true
        }
    }
    
    // Fetch order details
    LaunchedEffect(orderId) {
        scope.launch {
            isLoading = true
            errorMessage = null
            val result = ordersRepository.getOrderById(orderId)
            result.onSuccess { orderData ->
                order = orderData
                isLoading = false
            }.onFailure { exception ->
                errorMessage = exception.message ?: "Failed to load order"
                isLoading = false
            }
        }
    }
    
    // Content without Scaffold - will use parent's app bar
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Loading order details...",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        errorMessage != null -> {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
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
        order != null -> {
            OrderDetailContent(
                order = order!!,
                ordersRepository = ordersRepository,
                onOrderUpdated = { updatedOrder ->
                    order = updatedOrder
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun OrderDetailContent(
    order: Order,
    ordersRepository: OrdersRepository,
    onOrderUpdated: (Order) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var showStatusDialog by remember { mutableStateOf(false) }
    var showPaymentStatusDialog by remember { mutableStateOf(false) }
    var isUpdating by remember { mutableStateOf(false) }
    var updateError by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Order #${order.orderNumber}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    StatusBadge(status = order.status)
                    PaymentStatusBadge(status = order.payment?.status)
                }
                if (order.createdAt != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Created on ${DateUtils.formatDateTime(order.createdAt)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        val items = order.items.orEmpty()
        if (items.isNotEmpty()) {
            DetailSection(title = "Order Items (${items.size})") {
                items.forEachIndexed { index, item ->
                    OrderItemRow(item = item)
                    if (index < items.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }

        if (order.customer != null) {
            DetailSection(title = "Customer Information") {
                DetailRow("Name", order.customer.name ?: "N/A")
                if (order.customer.phone != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Phone:",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = order.customer.phone,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { makePhoneCall(context, order.customer.phone) },
                                modifier = Modifier.size(48.dp),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = Color(0xFF4CAF50),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Phone,
                                    contentDescription = "Call customer",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
                DetailRow("Email", order.customer.email?.takeIf { it.isNotBlank() } ?: "N/A")
                if (!order.customer.gstNumber.isNullOrBlank()) {
                    DetailRow("GST Number", order.customer.gstNumber)
                }
            }
        }

        DetailSection(title = "Order Information") {
            DetailRow("Order Type", order.orderType ?: "N/A")
            DetailRow("Source", order.source ?: "POS")
            DetailRow("Estimated Time", order.estimatedTime ?: "N/A")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = order.payment?.method ?: "N/A",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    PaymentStatusBadge(status = order.payment?.status)
                }
            }
            DetailRow(
                "Paid Amount",
                formatMoney(order.payment?.amount ?: order.total),
                valueColor = MaterialTheme.colorScheme.primary,
                valueFontWeight = FontWeight.Medium
            )
            if (!order.payment?.transactionId.isNullOrBlank()) {
                DetailRow("Txn ID", order.payment?.transactionId ?: "")
            }
            val deliveryBoy = order.deliveryBoy?.toString()?.takeIf { it.isNotBlank() && it != "null" }
            if (deliveryBoy != null) {
                DetailRow("Delivery Partner", deliveryBoy)
            }
        }

        OrderBillSummaryCard(order = order)

        if (order.orderType == "delivery" && order.deliveryAddress != null) {
            val address = order.deliveryAddress
            DetailSection(title = "Delivery Address") {
                if (!address.label.isNullOrBlank()) {
                    Text(
                        text = address.label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                val lineParts = listOfNotNull(
                    address.flatNo?.takeIf { it.isNotBlank() },
                    address.buildingName?.takeIf { it.isNotBlank() },
                    address.addressLine?.takeIf { it.isNotBlank() }
                )
                if (lineParts.isNotEmpty()) {
                    Text(
                        text = lineParts.joinToString(", "),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (!address.landmark.isNullOrBlank()) {
                    Text(
                        text = "Landmark: ${address.landmark}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val cityLine = listOfNotNull(
                    address.city?.takeIf { it.isNotBlank() },
                    address.state?.takeIf { it.isNotBlank() }
                ).joinToString(", ")
                val location = if (!address.pincode.isNullOrBlank()) {
                    if (cityLine.isNotEmpty()) "$cityLine - ${address.pincode}" else address.pincode
                } else {
                    cityLine
                }
                if (!location.isNullOrBlank()) {
                    Text(
                        text = location,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { openMapDirections(context, address) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Directions,
                        contentDescription = "Directions",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Get Directions")
                }
            }
        } else if (order.orderType == "takeaway" || order.orderType == "pickup") {
            DetailSection(title = "Pickup Address") {
                Text(
                    text = "Customer will pick up from restaurant",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (!order.notes.isNullOrEmpty()) {
            DetailSection(title = "Special Instructions") {
                Text(
                    text = order.notes,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val history = order.orderHistory.orEmpty()
        if (history.isNotEmpty()) {
            DetailSection(title = "Order History") {
                history.asReversed().forEachIndexed { index, entry ->
                    OrderHistoryRow(entry)
                    if (index < history.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Actions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                // Update Order Status Button
                Button(
                    onClick = { showStatusDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUpdating
                ) {
                    Text("Update Order Status")
                }
                
                // Update Payment Status Button
                Button(
                    onClick = { showPaymentStatusDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUpdating
                ) {
                    Text("Update Payment Status")
                }
                
                if (updateError != null) {
                    Text(
                        text = updateError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        }
        
        // Status Update Dialog
        if (showStatusDialog) {
            StatusUpdateDialog(
                currentStatus = order.status ?: "",
                onDismiss = { showStatusDialog = false },
                onConfirm = { newStatus ->
                    scope.launch {
                        isUpdating = true
                        updateError = null
                        val result = ordersRepository.updateOrderStatus(order._id, newStatus)
                        result.onSuccess { updatedData ->
                            // Refresh order data
                            ordersRepository.getOrderById(order._id).onSuccess { updatedOrder ->
                                onOrderUpdated(updatedOrder)
                            }
                            showStatusDialog = false
                            isUpdating = false
                        }.onFailure { exception ->
                            updateError = exception.message ?: "Failed to update status"
                            isUpdating = false
                        }
                    }
                }
            )
        }
        
        // Payment Status Update Dialog
        if (showPaymentStatusDialog) {
            PaymentStatusUpdateDialog(
                currentPaymentStatus = order.payment?.status ?: "",
                currentPaymentMethod = order.payment?.method ?: "",
                onDismiss = { showPaymentStatusDialog = false },
                onConfirm = { newPaymentStatus, newPaymentMethod ->
                    scope.launch {
                        isUpdating = true
                        updateError = null
                        val result = ordersRepository.updatePaymentStatus(
                            order._id,
                            newPaymentStatus,
                            newPaymentMethod
                        )
                        result.onSuccess { updatedData ->
                            // Refresh order data
                            ordersRepository.getOrderById(order._id).onSuccess { updatedOrder ->
                                onOrderUpdated(updatedOrder)
                            }
                            showPaymentStatusDialog = false
                            isUpdating = false
                        }.onFailure { exception ->
                            updateError = exception.message ?: "Failed to update payment status"
                            isUpdating = false
                        }
                    }
                }
            )
        }
    }
}

// Helper function to make phone call
fun makePhoneCall(context: android.content.Context, phoneNumber: String) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:$phoneNumber")
    }
    context.startActivity(intent)
}

// Helper function to open map directions
fun openMapDirections(context: android.content.Context, address: com.prashantpizza.nofsdotaca.repository.DeliveryAddress) {
    val intent = when {
        // If lat/lng are available, use Google Navigation
        address.lat != null && address.lng != null -> {
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("google.navigation:q=${address.lat},${address.lng}")
                setPackage("com.google.android.apps.maps")
            }
        }
        // Otherwise, use geo URI with address query
        else -> {
            val addressString = buildString {
                if (address.flatNo != null) append(address.flatNo)
                if (address.buildingName != null) {
                    if (isNotEmpty()) append(", ")
                    append(address.buildingName)
                }
                if (address.addressLine != null) {
                    if (isNotEmpty()) append(", ")
                    append(address.addressLine)
                }
                if (address.city != null) {
                    if (isNotEmpty()) append(", ")
                    append(address.city)
                }
                if (address.state != null) {
                    if (isNotEmpty()) append(", ")
                    append(address.state)
                }
                if (address.pincode != null) {
                    if (isNotEmpty()) append(" ")
                    append(address.pincode)
                }
            }
            if (addressString.isNotEmpty()) {
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("geo:0,0?q=${Uri.encode(addressString)}")
                }
            } else {
                null
            }
        }
    }
    
    if (intent != null) {
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to generic geo intent if Google Maps is not available
            val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:0,0?q=${Uri.encode(buildString {
                    if (address.flatNo != null) append(address.flatNo)
                    if (address.buildingName != null) {
                        if (isNotEmpty()) append(", ")
                        append(address.buildingName)
                    }
                    if (address.addressLine != null) {
                        if (isNotEmpty()) append(", ")
                        append(address.addressLine)
                    }
                    if (address.city != null) {
                        if (isNotEmpty()) append(", ")
                        append(address.city)
                    }
                    if (address.state != null) {
                        if (isNotEmpty()) append(", ")
                        append(address.state)
                    }
                    if (address.pincode != null) {
                        if (isNotEmpty()) append(" ")
                        append(address.pincode)
                    }
                })}")
            }
            context.startActivity(fallbackIntent)
        }
    }
}

@Composable
fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White // White card background
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueFontWeight: FontWeight = FontWeight.Normal,
    valueFontSize: androidx.compose.ui.unit.TextUnit = 14.sp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = valueFontSize,
            fontWeight = valueFontWeight,
            color = valueColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun OrderItemRow(item: OrderItem) {
    val offer = item.offer
    val hasOffer = !offer?.offerType.isNullOrBlank()
    val isFreeItem = offer?.isFreeItem == true
    val perUnitOriginal = offer?.originalPrice ?: item.basePrice ?: 0.0
    val perUnitOfferPrice = offer?.offerPrice ?: perUnitOriginal
    val quantity = item.quantity ?: 1
    val lineOriginalTotal = perUnitOriginal * quantity
    val lineFinalTotal = item.itemTotal ?: (perUnitOfferPrice * quantity)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.productName ?: "Item",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            if (hasOffer) {
                val title = when (offer?.offerType) {
                    "pizza_mania" -> "PIZZA MANIA"
                    "bogo" -> "BOGO"
                    "combo" -> "COMBO"
                    "custom_offer" -> offer.offerTitle?.takeIf { it.isNotBlank() } ?: "CUSTOM OFFER"
                    else -> offer?.offerTitle?.takeIf { it.isNotBlank() } ?: "Offer"
                }
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2F855A),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(Color(0xFFC6F6D5), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            OrderItemCustomizationChips(item)
            if (!item.specialInstructions.isNullOrEmpty()) {
                Text(
                    text = "Note: ${item.specialInstructions}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Qty: $quantity",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatMoney(perUnitOriginal),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isFreeItem) {
                Text(
                    text = formatMoney(lineOriginalTotal),
                    fontSize = 11.sp,
                    color = Color(0xFF718096),
                    style = androidx.compose.ui.text.TextStyle(
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    )
                )
                Text(
                    text = "Free",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F855A)
                )
            } else {
                Text(
                    text = formatMoney(lineFinalTotal),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String?) {
    val label = status?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: "Unknown"
    val background = when (status?.lowercase()) {
        "pending" -> Color(0xFFFEF3C7)
        "confirmed", "preparing" -> Color(0xFFDBEAFE)
        "ready", "out_for_delivery" -> Color(0xFFE0E7FF)
        "delivered", "completed" -> Color(0xFFD1FAE5)
        "cancelled", "rejected" -> Color(0xFFFEE2E2)
        else -> Color(0xFFE5E7EB)
    }
    val foreground = when (status?.lowercase()) {
        "pending" -> Color(0xFF92400E)
        "confirmed", "preparing" -> Color(0xFF1E40AF)
        "ready", "out_for_delivery" -> Color(0xFF3730A3)
        "delivered", "completed" -> Color(0xFF065F46)
        "cancelled", "rejected" -> Color(0xFF991B1B)
        else -> Color(0xFF374151)
    }
    Surface(shape = RoundedCornerShape(999.dp), color = background) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = foreground
        )
    }
}

@Composable
fun PaymentStatusBadge(status: String?) {
    val label = status?.replaceFirstChar { it.uppercase() } ?: "Pending"
    val background = when (status?.lowercase()) {
        "paid" -> Color(0xFFD1FAE5)
        "pending" -> Color(0xFFFEF3C7)
        "refunded" -> Color(0xFFDBEAFE)
        else -> Color(0xFFFEE2E2)
    }
    val foreground = when (status?.lowercase()) {
        "paid" -> Color(0xFF065F46)
        "pending" -> Color(0xFF92400E)
        "refunded" -> Color(0xFF1E40AF)
        else -> Color(0xFF991B1B)
    }
    Surface(shape = RoundedCornerShape(999.dp), color = background) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = foreground
        )
    }
}

@Composable
fun OrderHistoryRow(entry: OrderHistoryEntry) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                StatusBadge(status = entry.status)
                Text(
                    text = entry.action ?: "Status updated",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            if (!entry.timestamp.isNullOrBlank()) {
                Text(
                    text = DateUtils.formatDateTime(entry.timestamp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!entry.description.isNullOrBlank()) {
            Text(
                text = entry.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Source: ${entry.source ?: "POS"}",
                fontSize = 11.sp,
                color = Color(0xFF6B7280)
            )
            Text(
                text = "By: ${entry.performedBy?.userName?.takeIf { it.isNotBlank() } ?: "System"}",
                fontSize = 11.sp,
                color = Color(0xFF6B7280)
            )
        }
        if (!entry.notes.isNullOrBlank()) {
            Text(
                text = "Note: ${entry.notes}",
                fontSize = 11.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
fun OrderBillSummaryCard(order: Order) {
    val bill = buildOrderBillBreakdown(order)
    var isExpanded by remember { mutableStateOf(true) }
    val gstLabel = when {
        !bill.gst.gstEnabled -> "GST (Disabled)"
        bill.gst.gstExempt -> "GST (Exempt)"
        else -> "GST (${bill.gstRate.toInt()}% on ${formatMoney(bill.gst.taxableAmount)})"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD7FCE6))
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF38A169), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Receipt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bill Summary",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF276749)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (bill.originalTotal > bill.total) {
                            Text(
                                text = formatMoney(bill.originalTotal),
                                fontSize = 14.sp,
                                color = Color(0x99276749),
                                style = androidx.compose.ui.text.TextStyle(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            )
                        }
                        Text(
                            text = formatMoney(bill.total),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF276749)
                        )
                    }
                    if (bill.savings > 0) {
                        Text(
                            text = "${formatMoney(bill.savings)} saved on the total!",
                            fontSize = 12.sp,
                            color = Color(0xFF276749)
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF38A169)
                )
            }

            if (isExpanded) {
                Column(modifier = Modifier.padding(16.dp)) {
                    BillAmountRow("Taxable Amount") {
                        Text(
                            text = formatMoney(bill.gst.taxableAmount),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D3748)
                        )
                    }
                    BillAmountRow("Non-GST Amount") {
                        Text(
                            text = formatMoney(bill.gst.nonTaxableAmount),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D3748)
                        )
                    }
                    BillAmountRow("Item Total") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (bill.originalItemTotal > bill.itemTotal) {
                                Text(
                                    text = formatMoney(bill.originalItemTotal),
                                    fontSize = 13.sp,
                                    color = Color(0xFF718096),
                                    style = androidx.compose.ui.text.TextStyle(
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    )
                                )
                            }
                            Text(
                                text = formatMoney(bill.itemTotal),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF48BB78)
                            )
                        }
                    }
                    BillAmountRow(order.deliveryFeeName ?: "Delivery Fee") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (bill.deliveryFee == 0.0 && bill.originalDeliveryFee > 0) {
                                Text(
                                    text = formatMoney(bill.originalDeliveryFee),
                                    fontSize = 13.sp,
                                    color = Color(0xFF718096),
                                    style = androidx.compose.ui.text.TextStyle(
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    )
                                )
                            }
                            Text(
                                text = if (bill.deliveryFee == 0.0) "FREE" else formatMoney(bill.deliveryFee),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (bill.deliveryFee == 0.0) Color(0xFF48BB78) else Color(0xFF2D3748)
                            )
                        }
                    }
                    if (bill.convenienceFee > 0) {
                        BillAmountRow(bill.convenienceFeeName) {
                            Text(
                                text = formatMoney(bill.convenienceFee),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2D3748)
                            )
                        }
                    }
                    if (bill.discount > 0) {
                        val discountLabel = if (bill.couponDiscount > 0) bill.couponLabel else "Discount"
                        BillAmountRow(discountLabel) {
                            Text(
                                text = "- ${formatMoney(bill.discount)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF48BB78)
                            )
                        }
                    }
                    if (bill.couponDiscount > 0 && bill.discount != bill.couponDiscount) {
                        BillAmountRow(bill.couponLabel) {
                            Text(
                                text = "- ${formatMoney(bill.couponDiscount)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF48BB78)
                            )
                        }
                    }
                    BillAmountRow(gstLabel) {
                        Text(
                            text = formatMoney(if (!bill.gst.gstEnabled || bill.gst.gstExempt) 0.0 else bill.tax),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D3748)
                        )
                    }
                    if (bill.gst.gstEnabled && !bill.gst.gstExempt && bill.gst.gstAppliedItems.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .padding(start = 8.dp, bottom = 12.dp)
                                .fillMaxWidth()
                        ) {
                            bill.gst.gstAppliedItems.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "+ ${item.name}",
                                        fontSize = 12.sp,
                                        color = Color(0xFFD96100),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                    if (bill.gst.gstEnabled && !bill.gst.gstExempt && bill.gst.gstIncludedItems.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .padding(start = 8.dp, bottom = 12.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Items with GST included:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2E8B57)
                            )
                            bill.gst.gstIncludedItems.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "- ${item.name}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF2E8B57),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = formatMoney(item.amount),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E8B57)
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748)
                        )
                        Text(
                            text = formatMoney(bill.total),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BillAmountRow(
    label: String,
    value: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF4A5568),
            modifier = Modifier.weight(1f)
        )
        value()
    }
}

@Composable
fun OrderItemCustomizationChips(item: OrderItem) {
    val sizeLabel = item.selectedSize?.size
    val crustLabel = item.selectedCrust?.crust
    val hasToppings = item.selectedToppings.orEmpty().any { !it.topping.isNullOrBlank() }
    val hasAddOns = item.selectedAddOns.orEmpty().any { !it.addOn.isNullOrBlank() }

    if (sizeLabel.isNullOrBlank() && crustLabel.isNullOrBlank() && !hasToppings && !hasAddOns) {
        return
    }

    Column(
        modifier = Modifier.padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (!sizeLabel.isNullOrBlank()) {
            CustomizationChipRow {
                CustomizationChip(
                    text = "Size: $sizeLabel",
                    background = Color(0xFFE2E8F0),
                    foreground = Color(0xFF2D3748)
                )
            }
        }
        if (!crustLabel.isNullOrBlank()) {
            CustomizationChipRow {
                CustomizationChip(
                    text = "Crust: $crustLabel${extraPrice(item.selectedCrust?.price)}",
                    background = Color(0xFFFEF3C7),
                    foreground = Color(0xFF92400E)
                )
            }
        }
        if (hasToppings) {
            CustomizationChipRow(label = "Toppings") {
                item.selectedToppings.orEmpty().forEach { topping ->
                    val name = topping.topping?.takeIf { it.isNotBlank() } ?: return@forEach
                    CustomizationChip(
                        text = "${name}${extraPrice(topping.price)}",
                        background = Color(0xFFFFEDD5),
                        foreground = Color(0xFF9A3412)
                    )
                }
            }
        }
        if (hasAddOns) {
            CustomizationChipRow(label = "Add-ons") {
                item.selectedAddOns.orEmpty().forEach { addon ->
                    val name = addon.addOn?.takeIf { it.isNotBlank() } ?: return@forEach
                    CustomizationChip(
                        text = "${name}${extraPrice(addon.price)}",
                        background = Color(0xFFD1FAE5),
                        foreground = Color(0xFF065F46)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomizationChipRow(
    label: String? = null,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B7280)
            )
        }
        content()
    }
}

private fun extraPrice(price: Double?): String {
    val value = price ?: 0.0
    return if (value > 0) " +₹${value.toInt()}" else ""
}

@Composable
fun CustomizationChip(
    text: String,
    background: Color,
    foreground: Color
) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = foreground,
        modifier = Modifier
            .background(background, RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusUpdateDialog(
    currentStatus: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val statuses = listOf(
        "pending",
        "confirmed",
        "preparing",
        "ready",
        "out_for_delivery",
        "delivered",
        "cancelled"
    )
    
    var selectedStatus by remember { mutableStateOf(currentStatus) }
    var expanded by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Update Order Status",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedStatus.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        statuses.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedStatus = status
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onConfirm(selectedStatus) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Update")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentStatusUpdateDialog(
    currentPaymentStatus: String,
    currentPaymentMethod: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    val paymentStatuses = listOf("paid", "pending", "refunded", "failed")
    val paymentMethods = listOf("cash", "online", "card", "qr-code")
    
    var selectedPaymentStatus by remember { mutableStateOf(currentPaymentStatus) }
    var selectedPaymentMethod by remember { mutableStateOf(currentPaymentMethod) }
    var statusExpanded by remember { mutableStateOf(false) }
    var methodExpanded by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Update Payment Status",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedPaymentStatus.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        paymentStatuses.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedPaymentStatus = status
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }
                
                ExposedDropdownMenuBox(
                    expanded = methodExpanded,
                    onExpandedChange = { methodExpanded = !methodExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedPaymentMethod.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method (Optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = methodExpanded,
                        onDismissRequest = { methodExpanded = false }
                    ) {
                        paymentMethods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedPaymentMethod = method
                                    methodExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onConfirm(selectedPaymentStatus, selectedPaymentMethod.takeIf { it.isNotEmpty() }) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Update")
                    }
                }
            }
        }
    }
}

