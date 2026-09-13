package com.prashantpizza.nofsdotaca.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prashantpizza.nofsdotaca.BuildConfig
import com.prashantpizza.nofsdotaca.repository.SupplyCatalogItem
import com.prashantpizza.nofsdotaca.repository.SupplyOrderLinePayload
import com.prashantpizza.nofsdotaca.repository.SupplyOrderSummary
import com.prashantpizza.nofsdotaca.repository.SupplyRepository
import kotlinx.coroutines.launch

/**
 * Supply POS tab — catalog + cart + my orders + receive.
 * Replaces pizza POS placeholder; still 4 tabs only (no 5th tab).
 */
@Composable
fun POSScreen() {
    val context = LocalContext.current
    val repo = remember { SupplyRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    var tab by remember { mutableIntStateOf(0) } // 0 catalog, 1 orders
    var query by remember { mutableStateOf("") }
    var catalog by remember { mutableStateOf<List<SupplyCatalogItem>>(emptyList()) }
    var orders by remember { mutableStateOf<List<SupplyOrderSummary>>(emptyList()) }
    var cart by remember { mutableStateOf<Map<String, Pair<SupplyCatalogItem, Double>>>(emptyMap()) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var selectedOrder by remember { mutableStateOf<SupplyOrderSummary?>(null) }

    fun loadCatalog() {
        scope.launch {
            loading = true
            repo.getCatalog(query.ifBlank { null })
                .onSuccess { catalog = it }
                .onFailure { message = it.message }
            loading = false
        }
    }

    fun loadOrders() {
        scope.launch {
            loading = true
            repo.listOrders()
                .onSuccess { orders = it }
                .onFailure { message = it.message }
            loading = false
        }
    }

    LaunchedEffect(tab) {
        if (tab == 0) loadCatalog() else loadOrders()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "Supply Order",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Order HQ inventory for this outlet",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        TabRow(selectedTabIndex = tab) {
            Tab(
                selected = tab == 0,
                onClick = { tab = 0 },
                text = { Text("Catalog") }
            )
            Tab(
                selected = tab == 1,
                onClick = { tab = 1 },
                text = { Text("My orders") }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        val errorMessage = message
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (tab == 0) {
            SupplyCatalogPane(
                query = query,
                onQueryChange = { query = it },
                onSearch = { loadCatalog() },
                loading = loading,
                catalog = catalog,
                cart = cart,
                onCartChange = { cart = it },
                onSubmit = {
                    scope.launch {
                        loading = true
                        message = null
                        val lines = cart.map { (id, pair) ->
                            SupplyOrderLinePayload(inventoryId = id, qty = pair.second)
                        }
                        if (lines.size > 50) {
                            message = "Max 50 lines"
                            loading = false
                            return@launch
                        }
                        repo.createOrder(lines)
                            .onSuccess {
                                message = "Order submitted (${it.orderNumber ?: it._id})"
                                cart = emptyMap()
                                tab = 1
                                loadOrders()
                            }
                            .onFailure { message = it.message }
                        loading = false
                    }
                }
            )
        } else {
            SupplyOrdersPane(
                loading = loading,
                orders = orders,
                selectedOrder = selectedOrder,
                onSelectOrder = { orderId ->
                    scope.launch {
                        repo.getOrder(orderId)
                            .onSuccess { selectedOrder = it }
                            .onFailure { message = it.message }
                    }
                },
                onClearSelection = { selectedOrder = null },
                onReceive = { orderId ->
                    scope.launch {
                        loading = true
                        repo.receiveOrder(orderId)
                            .onSuccess {
                                message = "Received"
                                selectedOrder = it
                                loadOrders()
                            }
                            .onFailure { message = it.message }
                        loading = false
                    }
                },
                onOpenPdf = { url, title ->
                    val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) }
                    context.startActivity(Intent.createChooser(intent, title))
                }
            )
        }
    }
}

@Composable
private fun SupplyCatalogPane(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    loading: Boolean,
    catalog: List<SupplyCatalogItem>,
    cart: Map<String, Pair<SupplyCatalogItem, Double>>,
    onCartChange: (Map<String, Pair<SupplyCatalogItem, Double>>) -> Unit,
    onSubmit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search") },
            singleLine = true
        )
        Button(
            onClick = onSearch,
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            Text("Search")
        }
        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(catalog, key = { it._id }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name.orEmpty(),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "₹${item.sellPrice ?: 0}/${item.unit.orEmpty()} · GST ${item.gstRate ?: 18}%",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = {
                                val currentQty = cart[item._id]?.second ?: 0.0
                                onCartChange(cart + (item._id to (item to (currentQty + 1.0))))
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                }
            }
        }

        if (cart.isNotEmpty()) {
            HorizontalDivider()
            Text(
                text = "Cart (${cart.size})",
                fontWeight = FontWeight.SemiBold
            )
            val cartLines = cart.values.toList()
            for ((item, qty) in cartLines) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name.orEmpty(),
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp
                    )
                    IconButton(
                        onClick = {
                            val next = qty - 1
                            onCartChange(
                                if (next <= 0) cart - item._id
                                else cart + (item._id to (item to next))
                            )
                        }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    Text(
                        text = qty.toInt().toString(),
                        modifier = Modifier.width(28.dp)
                    )
                    IconButton(
                        onClick = {
                            onCartChange(cart + (item._id to (item to (qty + 1))))
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                    IconButton(onClick = { onCartChange(cart - item._id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                    }
                }
            }
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                Text("Submit supply order")
            }
        }
    }
}

@Composable
private fun SupplyOrdersPane(
    loading: Boolean,
    orders: List<SupplyOrderSummary>,
    selectedOrder: SupplyOrderSummary?,
    onSelectOrder: (String) -> Unit,
    onClearSelection: () -> Unit,
    onReceive: (String) -> Unit,
    onOpenPdf: (url: String, title: String) -> Unit
) {
    if (selectedOrder != null) {
        val order = selectedOrder
        Column(modifier = Modifier.fillMaxSize()) {
            Button(onClick = onClearSelection) {
                Text("← Back")
            }
            Text(
                text = order.orderNumber ?: order._id,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(text = "Status: ${order.status}")
            Text(text = "Total: ₹${order.total ?: 0}")
            Spacer(modifier = Modifier.height(8.dp))

            val lines = order.lines.orEmpty()
            for (line in lines) {
                Text(
                    text = "${line.name}: d=${line.dispatchedQty} r=${line.receivedQty} @ ₹${line.sellPrice}",
                    fontSize = 13.sp
                )
            }

            if (order.status == "dispatched") {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onReceive(order._id) },
                    enabled = !loading
                ) {
                    Text("Confirm receive")
                }
            }

            if (order.status in listOf("approved", "dispatched", "received", "invoiced")) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        onOpenPdf(
                            "${BuildConfig.API_BASE_URL}supply/orders/${order._id}/po-pdf",
                            "Open PO PDF"
                        )
                    }
                ) {
                    Text("Open / share PO PDF")
                }
            }
            if (order.status == "invoiced") {
                TextButton(
                    onClick = {
                        onOpenPdf(
                            "${BuildConfig.API_BASE_URL}supply/orders/${order._id}/invoice-pdf",
                            "Open invoice PDF"
                        )
                    }
                ) {
                    Text("Open / share invoice PDF")
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(orders, key = { it._id }) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectOrder(order._id) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = order.orderNumber ?: order._id.takeLast(8),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${order.status} · ₹${order.total ?: 0}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
