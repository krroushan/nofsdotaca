package com.serqfix.partner.data.api

import retrofit2.http.*

// Request models
data class UpdateAvailabilityRequest(
    val providerId: String,
    val available: Boolean
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

interface AvailabilityApiService {
    @POST("mobile-app/availability/toggle")
    suspend fun toggleAvailability(@Body request: UpdateAvailabilityRequest): AvailabilityResponse
    
    @GET("mobile-app/availability/{providerId}")
    suspend fun getAvailabilityStatus(@Path("providerId") providerId: String): AvailabilityResponse
}
