package com.serqfix.partner.data.api

import retrofit2.http.*

// Request models
data class PunchAttendanceRequest(
    val providerId: String,
    val action: String, // "login" or "logout"
    val location: com.serqfix.partner.data.api.LocationData? = null
)

// LocationData is already defined in BookingApiService, reusing it

// Response models
data class AttendanceResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<AttendanceRecord>? = null
)

data class AttendanceRecord(
    val _id: String? = null,
    val providerId: String? = null,
    val date: String? = null,
    val login_time: String? = null,
    val logout_time: String? = null,
    val total_hours: Double? = null,
    val status: String? = null // present, half-day, leave, absent
)

data class HolidayCheckResponse(
    val success: Boolean,
    val isHoliday: Boolean? = null,
    val holiday: String? = null,
    val message: String? = null
)

interface AttendanceApiService {
    @GET("mobile-app/attendance")
    suspend fun fetchProviderAttendance(
        @Query("providerId") providerId: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): AttendanceResponse
    
    @POST("mobile-app/attendance/punch")
    suspend fun punchAttendance(@Body request: PunchAttendanceRequest): AttendanceResponse
    
    @GET("mobile-app/attendance/is-holiday")
    suspend fun checkIsHoliday(@Query("date") date: String): HolidayCheckResponse
}
