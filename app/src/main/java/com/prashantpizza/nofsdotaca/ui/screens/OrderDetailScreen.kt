package com.prashantpizza.nofsdotaca.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.prashantpizza.nofsdotaca.repository.Order
import com.prashantpizza.nofsdotaca.repository.OrdersRepository
import com.prashantpizza.nofsdotaca.utils.DateUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

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
            .background(Color(0xFFF5F5F5)) // Light gray background
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Order Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                        fontSize = 24.sp,
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
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Order type and source
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (order.orderType != null) {
                        Text(
                            text = "Type: ${order.orderType.replaceFirstChar { it.uppercase() }}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (order.source != null) {
                        Text(
                            text = "• Source: ${order.source.replaceFirstChar { it.uppercase() }}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Timestamps
                if (order.createdAt != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Placed: ${DateUtils.formatDate(order.createdAt)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (order.updatedAt != null && order.updatedAt != order.createdAt) {
                    Text(
                        text = "Updated: ${DateUtils.formatDate(order.updatedAt)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Customer Information
        if (order.customer != null) {
            DetailSection(
                title = "Customer Information"
            ) {
                if (order.customer.name != null) {
                    DetailRow("Name", order.customer.name)
                }
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
                                    containerColor = Color(0xFF4CAF50), // Green background
                                    contentColor = Color.White // White icon
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
                if (order.customer.email != null) {
                    DetailRow("Email", order.customer.email)
                }
            }
        }
        
        // Order Items
        if (!order.items.isNullOrEmpty()) {
            DetailSection(
                title = "Order Items (${order.items.size})"
            ) {
                order.items.forEachIndexed { index, item ->
                    OrderItemRow(item = item)
                    if (index < order.items.size - 1) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
        
        // Delivery/Pickup Address
        if (order.orderType == "delivery" && order.deliveryAddress != null) {
            DetailSection(
                title = "Delivery Address"
            ) {
                val address = order.deliveryAddress
                if (address.label != null) {
                    DetailRow("Label", address.label)
                }
                if (address.addressLine != null) {
                    DetailRow("Address", address.addressLine)
                }
                if (address.landmark != null) {
                    DetailRow("Landmark", address.landmark)
                }
                if (address.city != null || address.state != null) {
                    val location = listOfNotNull(address.city, address.state).joinToString(", ")
                    if (location.isNotEmpty()) {
                        DetailRow("Location", location)
                    }
                }
                if (address.pincode != null) {
                    DetailRow("Pincode", address.pincode)
                }
                
                // Map Directions Button
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
        } else if (order.orderType == "takeaway" && order.pickupAddress != null) {
            DetailSection(
                title = "Pickup Address"
            ) {
                Text(
                    text = "Customer will pick up from restaurant",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Payment Information
        if (order.payment != null) {
            DetailSection(
                title = "Payment Information"
            ) {
                if (order.payment.status != null) {
                    DetailRow(
                        "Status",
                        order.payment.status.replaceFirstChar { it.uppercase() },
                        valueColor = when (order.payment.status.lowercase()) {
                            "paid" -> MaterialTheme.colorScheme.primary
                            "pending" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                if (order.payment.method != null) {
                    DetailRow("Method", order.payment.method.replaceFirstChar { it.uppercase() })
                }
                if (order.payment.amount != null) {
                    DetailRow(
                        "Amount",
                        "₹${String.format("%.2f", order.payment.amount)}",
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                }
                if (order.payment.transactionId != null && order.payment.transactionId.isNotEmpty()) {
                    DetailRow("Transaction ID", order.payment.transactionId)
                }
            }
        }
        
        // Action Buttons
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White // White card background
            )
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
        
        // Order Summary
        DetailSection(
            title = "Order Summary"
        ) {
            if (order.subtotal != null) {
                DetailRow("Subtotal", "₹${String.format("%.2f", order.subtotal)}")
            }
            if (order.discount != null && order.discount!! > 0) {
                DetailRow("Discount", "-₹${String.format("%.2f", order.discount)}")
            }
            if (order.tax != null && order.tax!! > 0) {
                DetailRow("Tax (GST ${order.gstRate ?: 0}%)", "₹${String.format("%.2f", order.tax)}")
            }
            if (order.deliveryFee != null && order.deliveryFee!! > 0) {
                DetailRow(
                    order.deliveryFeeName ?: "Delivery Fee",
                    "₹${String.format("%.2f", order.deliveryFee)}"
                )
            }
            if (order.convenienceFee != null && order.convenienceFee!! > 0) {
                DetailRow(
                    order.convenienceFeeName ?: "Convenience Fee",
                    "₹${String.format("%.2f", order.convenienceFee)}"
                )
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            if (order.total != null) {
                DetailRow(
                    "Total",
                    "₹${String.format("%.2f", order.total)}",
                    valueColor = MaterialTheme.colorScheme.primary,
                    valueFontWeight = FontWeight.Bold,
                    valueFontSize = 18.sp
                )
            }
        }
        
        // Estimated Time
        if (order.estimatedTime != null) {
            DetailSection(
                title = "Estimated Time"
            ) {
                Text(
                    text = order.estimatedTime,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Notes
        if (!order.notes.isNullOrEmpty()) {
            DetailSection(
                title = "Notes"
            ) {
                Text(
                    text = order.notes,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                if (address.addressLine != null) append(address.addressLine)
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
                    if (address.addressLine != null) append(address.addressLine)
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
fun OrderItemRow(item: com.prashantpizza.nofsdotaca.repository.OrderItem) {
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
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Qty: ${item.quantity ?: 1}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "₹${String.format("%.2f", item.itemTotal ?: 0.0)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun OrderItemCustomizationChips(item: com.prashantpizza.nofsdotaca.repository.OrderItem) {
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

