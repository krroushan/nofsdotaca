package com.serqfix.partner.data.api

import retrofit2.http.*

// Request models
data class UpdateAvailabilityRequest(
    val providerId: String,
    val available: Boolean
)

data class OnOffDutyRequest(
    val providerId: String,
    val isOnDuty: Boolean
)

// Response models
data class AvailabilityResponse(
    val success: Boolean,
    val message: String? = null,
    val data: AvailabilityData? = null
)

data class AvailabilityData(
    val available: Boolean? = null,
    val updatedAt: String? = null
)

data class DutyStatusResponse(
    val success: Boolean,
    val message: String? = null,
    val data: DutyStatusData? = null
)

data class DutyStatusData(
    val isOnDuty: Boolean? = null
)

interface AvailabilityApiService {
    @POST("mobile-app/availability/toggle")
    suspend fun toggleAvailability(@Body request: UpdateAvailabilityRequest): AvailabilityResponse
    
    @GET("mobile-app/availability/{providerId}")
    suspend fun getAvailabilityStatus(@Path("providerId") providerId: String): AvailabilityResponse
    
    // On-Off Duty endpoints (matching React Native)
    @POST("mobile-app/on-off-duty")
    suspend fun onOffDuty(@Body request: OnOffDutyRequest): DutyStatusResponse
    
    @GET("mobile-app/on-off-duty")
    suspend fun getDutyStatus(@Query("providerId") providerId: String): DutyStatusResponse
}
