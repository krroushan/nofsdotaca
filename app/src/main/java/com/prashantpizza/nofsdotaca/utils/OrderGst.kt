package com.prashantpizza.nofsdotaca.utils

import com.prashantpizza.nofsdotaca.repository.Order
import com.prashantpizza.nofsdotaca.repository.OrderItem
import java.util.Locale
import kotlin.math.max

data class GstLineItem(
    val name: String,
    val amount: Double
)

data class OrderGstBreakdown(
    val gstEnabled: Boolean,
    val gstExempt: Boolean,
    val gstAppliedItems: List<GstLineItem>,
    val gstIncludedItems: List<GstLineItem>,
    val taxableAmount: Double,
    val nonTaxableAmount: Double
)

data class OrderBillBreakdown(
    val itemTotal: Double,
    val originalItemTotal: Double,
    val deliveryFee: Double,
    val originalDeliveryFee: Double,
    val convenienceFee: Double,
    val convenienceFeeName: String,
    val discount: Double,
    val couponDiscount: Double,
    val couponLabel: String,
    val tax: Double,
    val gstRate: Double,
    val total: Double,
    val originalTotal: Double,
    val savings: Double,
    val gst: OrderGstBreakdown
)

fun roundMoney(value: Double?): Double {
    return String.format(Locale.US, "%.2f", value ?: 0.0).toDouble()
}

fun formatMoney(value: Double?): String {
    return "₹${String.format(Locale.US, "%.2f", value ?: 0.0)}"
}

fun resolveItemGstType(item: OrderItem): String {
    return if (item.gstType == "inclusive") "inclusive" else "exclusive"
}

fun buildOrderGstBreakdown(order: Order): OrderGstBreakdown {
    val items = order.items.orEmpty()
    val gstEnabled = order.gstEnabled != false
    val gstExempt = order.gstExempt == true
    val tax = roundMoney(order.tax)

    val exclusive = mutableListOf<GstLineItem>()
    val inclusive = mutableListOf<GstLineItem>()

    items.forEach { item ->
        val amount = roundMoney(item.itemTotal)
        val name = item.productName?.takeIf { it.isNotBlank() } ?: "Item"
        if (resolveItemGstType(item) == "inclusive") {
            inclusive.add(GstLineItem(name, amount))
        } else {
            exclusive.add(GstLineItem(name, amount))
        }
    }

    val taxableAmount = roundMoney(exclusive.sumOf { it.amount })
    val nonTaxableAmount = roundMoney(inclusive.sumOf { it.amount })
    val exclusiveTotal = taxableAmount

    val gstAppliedItems =
        if (gstEnabled && !gstExempt && tax > 0) {
            exclusive
                .filter { it.amount > 0 }
                .map { item ->
                    GstLineItem(
                        name = item.name,
                        amount = if (exclusiveTotal > 0) {
                            roundMoney((item.amount / exclusiveTotal) * tax)
                        } else {
                            0.0
                        }
                    )
                }
        } else {
            emptyList()
        }

    return OrderGstBreakdown(
        gstEnabled = gstEnabled,
        gstExempt = gstExempt,
        gstAppliedItems = gstAppliedItems,
        gstIncludedItems = inclusive,
        taxableAmount = taxableAmount,
        nonTaxableAmount = nonTaxableAmount
    )
}

fun buildOrderBillBreakdown(order: Order): OrderBillBreakdown {
    val itemTotal = roundMoney(order.subtotal)
    val deliveryFee = roundMoney(order.deliveryFee)
    val convenienceFee = roundMoney(order.convenienceFee)
    val discount = roundMoney(order.discount)
    val couponDiscount = roundMoney(order.coupon?.discount)
    val tax = roundMoney(order.tax)
    val total = roundMoney(order.total)
    val gstRate = order.gstRate ?: 0.0
    val gst = buildOrderGstBreakdown(order)
    val isDelivery = order.orderType == "delivery"

    val originalItemTotal = roundMoney(itemTotal + discount)
    val originalDeliveryFee = if (isDelivery && deliveryFee == 0.0) 50.0 else deliveryFee
    val originalTotal = roundMoney(originalItemTotal + originalDeliveryFee + convenienceFee + tax)
    val savings = roundMoney(max(0.0, originalTotal - total))

    val couponLabel = order.coupon?.name?.takeIf { it.isNotBlank() }
        ?: order.coupon?.code?.takeIf { it.isNotBlank() }?.let { "Coupon ($it)" }
        ?: "Discount"

    return OrderBillBreakdown(
        itemTotal = itemTotal,
        originalItemTotal = originalItemTotal,
        deliveryFee = deliveryFee,
        originalDeliveryFee = originalDeliveryFee,
        convenienceFee = convenienceFee,
        convenienceFeeName = order.convenienceFeeName?.takeIf { it.isNotBlank() } ?: "Convenience Fee",
        discount = discount,
        couponDiscount = couponDiscount,
        couponLabel = couponLabel,
        tax = tax,
        gstRate = gstRate,
        total = total,
        originalTotal = originalTotal,
        savings = savings,
        gst = gst
    )
}
