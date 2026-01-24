package com.serqfix.partner.data.api

import retrofit2.http.*

// Response models
data class DashboardResponse(
    val success: Boolean,
    val message: String? = null,
    val data: DashboardData? = null
)

data class DashboardData(
    val stats: DashboardStats? = null,
    val charts: DashboardCharts? = null
)

data class DashboardStats(
    val totalEarnings: Double? = null,
    val currentMonthEarnings: Double? = null,
    val totalBookings: Int? = null,
    val completedBookings: Int? = null,
    val pendingBookings: Int? = null,
    val rating: Double? = null,
    val reviewCount: Int? = null,
    val pendingRevenue: Double? = null
)

data class DashboardCharts(
    val earningsPerDay: List<ChartDataPoint>? = null,
    val earningsPerDayFromBookings: List<ChartDataPoint>? = null,
    val bookingsPerDay: List<ChartDataPoint>? = null
)

data class ChartDataPoint(
    val date: String? = null,
    val amount: Double? = null,
    val count: Int? = null
)

interface DashboardApiService {
    @GET("mobile-app/dashboard/{providerId}")
    suspend fun getDashboardData(@Path("providerId") providerId: String): DashboardResponse
}
