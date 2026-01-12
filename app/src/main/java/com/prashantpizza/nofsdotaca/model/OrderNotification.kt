package com.prashantpizza.nofsdotaca.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Parcelize
data class OrderNotification(
    val orderId: String, // orderNumber (e.g., "PP20251852")
    val orderMongoId: String? = null, // MongoDB ObjectId (e.g., "696516776bd1c15ddd0a9154")
    val title: String,
    val body: String,
    val customerName: String,
    val items: String,
    val amount: String,
    val address: String,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {
    
    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    companion object {
        // Create dummy order for testing
        fun createDummyOrder(
            title: String = "New Order",
            body: String = "You have a new order!"
        ): OrderNotification {
            return OrderNotification(
                orderId = "ORD-${System.currentTimeMillis() % 100000}",
                title = title,
                body = body,
                customerName = "John Doe",
                items = "2x Pizza Margherita, 1x Coca Cola",
                amount = "$25.99",
                address = "123 Main Street, Apt 4B"
            )
        }
        
        // Parse from FCM data payload
        fun fromFCMData(data: Map<String, String>): OrderNotification {
            return OrderNotification(
                orderId = data["orderId"] ?: "ORD-${System.currentTimeMillis() % 100000}",
                orderMongoId = data["orderMongoId"], // MongoDB ObjectId if available
                title = data["title"] ?: "New Order",
                body = data["body"] ?: "You have a new order!",
                customerName = data["customerName"] ?: "John Doe",
                items = data["items"] ?: "2x Pizza, 1x Coke",
                amount = data["amount"] ?: "$25.99",
                address = data["address"] ?: "123 Main St",
                timestamp = data["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
            )
        }
    }
}
