package com.serqfix.partner.data.api

import retrofit2.http.*

// Request models
data class PaymentListRequest(
    val userId: String,
    val page: Int = 1,
    val limit: Int = 10,
    val status: String? = null
)

// Response models
data class PaymentResponse(
    val success: Boolean,
    val message: String? = null,
    val data: PaymentData? = null
)

data class PaymentData(
    val _id: String? = null,
    val bookingId: String? = null,
    val amount: Double? = null,
    val status: String? = null,
    val paymentDate: String? = null,
    val createdAt: String? = null
)

data class PaymentsListResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<PaymentData>? = null,
    val meta: MetaData? = null
)

interface PaymentApiService {
    @GET("mobile-app/payments/{userId}")
    suspend fun getPayments(
        @Path("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("status") status: String? = null
    ): PaymentsListResponse
    
    @GET("mobile-app/payment/{paymentId}")
    suspend fun getPaymentById(@Path("paymentId") paymentId: String): PaymentResponse
}
