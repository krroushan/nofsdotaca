package com.serqfix.partner.data.api

import retrofit2.http.*

// Request models
data class AcceptAMCAssignmentRequest(
    val orderId: String,
    val providerId: String
)

data class RejectAMCAssignmentRequest(
    val orderId: String,
    val providerId: String,
    val reason: String? = null
)

// Response models
data class AMCOrderResponse(
    val success: Boolean,
    val message: String? = null,
    val data: AMCOrderData? = null
)

data class AMCOrderData(
    val _id: String? = null,
    val orderId: String? = null,
    val customerName: String? = null,
    val serviceName: String? = null,
    val address: String? = null,
    val status: String? = null,
    val visits: List<AMCVisitData>? = null,
    val createdAt: String? = null
)

data class AMCVisitData(
    val _id: String? = null,
    val visitDate: String? = null,
    val visitTime: String? = null,
    val status: String? = null,
    val completedAt: String? = null
)

data class AMCOrdersListResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<AMCOrderData>? = null,
    val meta: MetaData? = null
)

data class AMCDashboardResponse(
    val success: Boolean,
    val message: String? = null,
    val data: AMCDashboardData? = null
)

data class AMCDashboardData(
    val totalOrders: Int? = null,
    val pendingAssignments: Int? = null,
    val activeOrders: Int? = null,
    val todayVisits: Int? = null
)

interface AMCApiService {
    @GET("mobile-app/amc/current")
    suspend fun getCurrentAMCOrder(@Query("providerId") providerId: String): AMCOrderResponse
    
    @GET("mobile-app/amc/dashboard")
    suspend fun getAMCDashboard(@Query("providerId") providerId: String): AMCDashboardResponse
    
    @GET("mobile-app/amc/assigned-orders/{providerId}")
    suspend fun getAssignedAMCOrders(
        @Path("providerId") providerId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("includeAll") includeAll: Boolean = false
    ): AMCOrdersListResponse
    
    @GET("mobile-app/amc/order/{orderId}")
    suspend fun getAMCOrderById(@Path("orderId") orderId: String): AMCOrderResponse
    
    @GET("mobile-app/amc/pending-assignments")
    suspend fun getPendingAMCAssignments(@Query("providerId") providerId: String): AMCOrdersListResponse
    
    @POST("mobile-app/amc/accept-assignment")
    suspend fun acceptAMCAssignment(@Body request: AcceptAMCAssignmentRequest): AMCOrderResponse
    
    @POST("mobile-app/amc/reject-assignment")
    suspend fun rejectAMCAssignment(@Body request: RejectAMCAssignmentRequest): AMCOrderResponse
}
